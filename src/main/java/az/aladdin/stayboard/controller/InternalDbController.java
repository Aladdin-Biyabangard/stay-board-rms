package az.aladdin.stayboard.controller;

import az.aladdin.stayboard.backup.SqlRestoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/rms/internal/db")
@RequiredArgsConstructor
public class InternalDbController {

    private final SqlRestoreService sqlRestoreService;

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SqlRestoreService.DbImportResult importDatabase(
            @RequestParam("file") MultipartFile file,
            @RequestParam("accessKey") String accessKey) {
        return sqlRestoreService.importDatabase(file, accessKey);
    }

    @PostMapping("/export")
    public ResponseEntity<byte[]> exportDatabase(@RequestParam("accessKey") String accessKey) {
        SqlRestoreService.DbExportResult result = sqlRestoreService.exportDatabase(accessKey);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(result.fileName())
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.parseMediaType("application/sql"))
                .contentLength(result.content().length)
                .body(result.content());
    }
}
