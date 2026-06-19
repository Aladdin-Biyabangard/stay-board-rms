package az.aladdin.stayboard.backup;

import az.aladdin.stayboard.config.DbBackupProperties;
import az.aladdin.stayboard.exception.BadRequestException;
import az.aladdin.stayboard.exception.MessageKey;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;

@Slf4j
@Service
@RequiredArgsConstructor
public class SqlRestoreService {

    private static final Pattern JDBC_MYSQL = Pattern.compile("jdbc:mysql://([^:/]+)(?::(\\d+))?/([^?]+)");
    private static final Pattern CREATE_DATABASE_STMT = Pattern.compile("(?i)^\\s*CREATE\\s+DATABASE\\b.*;\\s*$");
    private static final Pattern DROP_DATABASE_STMT = Pattern.compile("(?i)^\\s*DROP\\s+DATABASE\\b.*;\\s*$");
    private static final Pattern USE_DATABASE_STMT = Pattern.compile("(?i)^\\s*USE\\b.*;\\s*$");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/sql",
            "text/plain",
            "application/octet-stream"
    );

    private final DataSource dataSource;
    private final MysqlDumpService mysqlDumpService;
    private final DbBackupProperties properties;

    @Value("${DB_NAME:stay_board_rms}")
    private String configuredDbName;

    public DbExportResult exportDatabase(String accessKey) {
        validateAccessKey(accessKey);

        ZoneId zoneId = ZoneId.of(properties.getZone());
        String datePart = LocalDate.now(zoneId).toString();
        String database = resolveTargetDatabaseName();
        String fileName = "stayboard-rms-db-backup-" + datePart + ".sql";

        byte[] sql;
        try {
            sql = mysqlDumpService.dumpDatabase();
        } catch (Exception e) {
            log.error("RMS DB export failed", e);
            throw new BadRequestException(MessageKey.BACKUP_OPERATION_FAILED);
        }

        log.info("RMS DB export completed for database {} ({} bytes)", database, sql.length);
        return new DbExportResult(fileName, sql, database, sql.length);
    }

    public DbImportResult importDatabase(MultipartFile file, String accessKey) {
        validateAccessKey(accessKey);
        validateSqlFile(file);

        String database = resolveTargetDatabaseName();
        String sanitizedSql = sanitizeSql(readUtf8(file), database);
        executeSqlAgainstDatabase(sanitizedSql, database);

        log.info("RMS DB import completed for database {}", database);
        return new DbImportResult("SQL import completed successfully", database);
    }

    private void validateAccessKey(String accessKey) {
        if (accessKey == null || accessKey.isBlank()) {
            throw new BadRequestException(MessageKey.BACKUP_ACCESS_KEY_REQUIRED);
        }
        String configured = properties.getAccessKey();
        if (configured == null || configured.isBlank()) {
            throw new BadRequestException(MessageKey.BACKUP_ACCESS_KEY_NOT_CONFIGURED);
        }
        if (!MessageDigest.isEqual(accessKey.getBytes(StandardCharsets.UTF_8), configured.getBytes(StandardCharsets.UTF_8))) {
            throw new BadRequestException(MessageKey.BACKUP_ACCESS_KEY_INVALID);
        }
    }

    private void validateSqlFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException(MessageKey.BACKUP_SQL_FILE_REQUIRED);
        }
        String name = file.getOriginalFilename();
        if (name == null || !name.toLowerCase().endsWith(".sql")) {
            throw new BadRequestException(MessageKey.BACKUP_SQL_FILE_EXTENSION);
        }
        String contentType = file.getContentType();
        if (contentType != null && !contentType.isBlank() && !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BadRequestException(MessageKey.BACKUP_SQL_FILE_MIME);
        }
    }

    private String resolveTargetDatabaseName() {
        String fromEnv = configuredDbName != null ? configuredDbName.trim() : "";
        if (!fromEnv.isEmpty()) {
            return fromEnv;
        }
        if (!(dataSource instanceof HikariDataSource hk)) {
            throw new BadRequestException(MessageKey.BACKUP_OPERATION_FAILED);
        }
        Matcher m = JDBC_MYSQL.matcher(hk.getJdbcUrl());
        if (!m.find()) {
            throw new BadRequestException(MessageKey.BACKUP_OPERATION_FAILED);
        }
        return URLDecoder.decode(m.group(3), StandardCharsets.UTF_8);
    }

    private String readUtf8(MultipartFile file) {
        try {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new BadRequestException(MessageKey.BACKUP_OPERATION_FAILED);
        }
    }

    private String sanitizeSql(String sql, String targetDb) {
        StringBuilder sb = new StringBuilder(sql.length());
        for (String line : sql.split("\\R")) {
            if (CREATE_DATABASE_STMT.matcher(line).matches()
                    || DROP_DATABASE_STMT.matcher(line).matches()
                    || USE_DATABASE_STMT.matcher(line).matches()) {
                continue;
            }
            sb.append(line).append('\n');
        }
        sb.insert(0, "USE `" + targetDb + "`;\n");
        return sb.toString();
    }

    private void executeSqlAgainstDatabase(String sqlScript, String database) {
        if (!(dataSource instanceof HikariDataSource hk)) {
            throw new BadRequestException(MessageKey.BACKUP_OPERATION_FAILED);
        }
        Matcher m = JDBC_MYSQL.matcher(hk.getJdbcUrl());
        if (!m.find()) {
            throw new BadRequestException(MessageKey.BACKUP_OPERATION_FAILED);
        }
        String host = m.group(1);
        int port = m.group(2) != null ? Integer.parseInt(m.group(2)) : 3306;
        String username = hk.getUsername();
        String password = hk.getPassword() != null ? hk.getPassword() : "";

        Path cnf = null;
        Process process = null;
        try {
            cnf = Files.createTempFile("mysql-import-cnf-", ".cnf");
            String ini = "[client]\n"
                    + "host=" + host + "\n"
                    + "port=" + port + "\n"
                    + "user=" + username + "\n"
                    + "password=" + escapeOptionFileValue(password) + "\n";
            Files.writeString(cnf, ini, StandardCharsets.UTF_8);
            tryRestrictCnfPermissions(cnf);

            ProcessBuilder pb = new ProcessBuilder(
                    properties.getMysqlPath(),
                    "--defaults-extra-file=" + cnf.toAbsolutePath(),
                    "--default-character-set=utf8mb4",
                    "--database=" + database
            );
            process = pb.start();
            process.getOutputStream().write(sqlScript.getBytes(StandardCharsets.UTF_8));
            process.getOutputStream().close();
            final Process runningProcess = process;

            long timeoutSeconds = properties.getTimeoutSeconds();
            try (ExecutorService ex = Executors.newVirtualThreadPerTaskExecutor()) {
                Future<byte[]> stdoutFuture = ex.submit(() -> runningProcess.getInputStream().readAllBytes());
                Future<String> stderrFuture = ex.submit(() ->
                        new String(runningProcess.getErrorStream().readAllBytes(), StandardCharsets.UTF_8));

                String stderr = waitAndRead(runningProcess, stdoutFuture, stderrFuture, timeoutSeconds);
                if (runningProcess.exitValue() != 0) {
                    log.error("RMS mysql import failed. stderr: {}", stderr);
                    throw new BadRequestException(MessageKey.BACKUP_OPERATION_FAILED);
                }
            }
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            if (process != null) {
                process.destroyForcibly();
            }
            throw new BadRequestException(MessageKey.BACKUP_OPERATION_FAILED);
        } finally {
            if (cnf != null) {
                try {
                    Files.deleteIfExists(cnf);
                } catch (IOException ignored) {
                    // no-op
                }
            }
        }
    }

    private String waitAndRead(Process process, Future<byte[]> stdoutFuture, Future<String> stderrFuture, long timeoutSeconds)
            throws InterruptedException, TimeoutException, ExecutionException {
        try {
            stdoutFuture.get(timeoutSeconds, TimeUnit.SECONDS);
            String stderr = stderrFuture.get(timeoutSeconds, TimeUnit.SECONDS);
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new TimeoutException("mysql import process timed out");
            }
            return stderr;
        } catch (TimeoutException e) {
            stdoutFuture.cancel(true);
            stderrFuture.cancel(true);
            process.destroyForcibly();
            throw e;
        }
    }

    private static void tryRestrictCnfPermissions(Path cnf) {
        try {
            Files.setPosixFilePermissions(cnf, Set.of(OWNER_READ, OWNER_WRITE));
        } catch (Exception ignored) {
            // no-op
        }
    }

    private static String escapeOptionFileValue(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        if (!value.contains("\"") && !value.contains("\n") && !value.contains("\r")) {
            return "\"" + value.replace("\\", "\\\\") + "\"";
        }
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    public record DbExportResult(String fileName, byte[] content, String database, long bytes) {
    }

    public record DbImportResult(String message, String database) {
    }
}
