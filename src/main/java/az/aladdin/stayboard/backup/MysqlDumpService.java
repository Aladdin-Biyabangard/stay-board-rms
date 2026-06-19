package az.aladdin.stayboard.backup;

import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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
public class MysqlDumpService {

    private static final Pattern JDBC_MYSQL = Pattern.compile("jdbc:mysql://([^:/]+)(?::(\\d+))?/([^?]+)");

    private final DataSource dataSource;

    @Value("${scheduler.db-backup.mysqldump-path:mysqldump}")
    private String mysqldumpPath;

    @Value("${scheduler.db-backup.timeout-seconds:7200}")
    private long timeoutSeconds;

    public byte[] dumpDatabase() throws IOException, InterruptedException {
        if (!(dataSource instanceof HikariDataSource hk)) {
            throw new IllegalStateException("Expected HikariDataSource for mysqldump");
        }
        String jdbcUrl = hk.getJdbcUrl();
        String username = hk.getUsername();
        String password = hk.getPassword() != null ? hk.getPassword() : "";

        Matcher m = JDBC_MYSQL.matcher(jdbcUrl);
        if (!m.find()) {
            throw new IllegalArgumentException("Unsupported JDBC URL for mysqldump: " + maskPassword(jdbcUrl));
        }
        String host = m.group(1);
        int port = m.group(2) != null ? Integer.parseInt(m.group(2)) : 3306;
        String database = URLDecoder.decode(m.group(3), StandardCharsets.UTF_8);

        Path cnf = Files.createTempFile("mysqldump-cnf-", ".cnf");
        try {
            String ini = "[client]\n"
                    + "host=" + host + "\n"
                    + "port=" + port + "\n"
                    + "user=" + username + "\n"
                    + "password=" + escapeOptionFileValue(password) + "\n";
            Files.writeString(cnf, ini, StandardCharsets.UTF_8);
            tryRestrictCnfPermissions(cnf);

            List<String> command = new ArrayList<>();
            command.add(mysqldumpPath);
            command.add("--defaults-extra-file=" + cnf.toAbsolutePath());
            command.add("--single-transaction");
            command.add("--routines");
            command.add("--triggers");
            command.add("--events");
            command.add("--set-gtid-purged=OFF");
            command.add("--column-statistics=0");
            command.add("--default-character-set=utf8mb4");
            command.add("--databases");
            command.add(database);

            return runMysqldump(command);
        } finally {
            Files.deleteIfExists(cnf);
        }
    }

    private byte[] runMysqldump(List<String> command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        Process process = pb.start();
        process.getOutputStream().close();

        try (ExecutorService ex = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<byte[]> outFut = ex.submit(() -> process.getInputStream().readAllBytes());
            Future<String> errFut = ex.submit(() -> new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8));
            byte[] stdout;
            String stderr;
            try {
                stdout = outFut.get(timeoutSeconds, TimeUnit.SECONDS);
                stderr = errFut.get(timeoutSeconds, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                outFut.cancel(true);
                errFut.cancel(true);
                process.destroyForcibly();
                throw new IOException("mysqldump timed out after " + timeoutSeconds + "s", e);
            } catch (ExecutionException e) {
                process.destroyForcibly();
                Throwable c = e.getCause();
                if (c instanceof IOException ioe) {
                    throw ioe;
                }
                throw new IOException("mysqldump stream read failed", c);
            }

            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new IOException("mysqldump process did not exit in time");
            }

            if (process.exitValue() != 0) {
                log.error("mysqldump failed (exit {}). stderr: {}", process.exitValue(), stderr);
                throw new IOException("mysqldump exited with " + process.exitValue());
            }
            if (stdout.length == 0) {
                log.warn("mysqldump produced empty output; stderr: {}", stderr);
            }
            return stdout;
        }
    }

    private static void tryRestrictCnfPermissions(Path cnf) {
        try {
            Files.setPosixFilePermissions(cnf, Set.of(OWNER_READ, OWNER_WRITE));
        } catch (Exception ignored) {
            // Windows or non-POSIX FS: skip
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

    private static String maskPassword(String jdbcUrl) {
        return jdbcUrl.replaceAll("password=([^&]*)", "password=***");
    }
}
