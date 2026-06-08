package az.aladdin.stayboard.service.common;

import az.aladdin.stayboard.exception.ApiExceptions;
import az.aladdin.stayboard.exception.MessageKey;
import az.aladdin.stayboard.model.response.FileUploadResponse;
import az.aladdin.stayboard.util.ImageMimeTypes;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileLoadService {

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.region}")
    private String region;

    private final S3Client s3Client;

    public FileUploadResponse uploadFile(MultipartFile multipartFile, String id, String keyOfWhat) throws IOException {
        validateImageFile(multipartFile);
        String key = createKey(id, keyOfWhat, multipartFile);
        createAwsObject(multipartFile, key);
        return new FileUploadResponse(key, getPublicFileUrl(key));
    }

    public void deleteFileFromAws(String key) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        s3Client.deleteObject(deleteObjectRequest);
    }

    public void deleteByPublicUrl(String publicUrl) {
        String key = resolveKey(publicUrl);
        if (key != null) {
            deleteFileFromAws(key);
        }
    }

    public String resolveKey(String fileRef) {
        if (fileRef == null || fileRef.isBlank()) {
            return null;
        }
        if (fileRef.startsWith("public/") || fileRef.startsWith("private/")) {
            return fileRef;
        }
        return publicKeyFromPublicUrl(fileRef);
    }

    public String getPublicFileUrl(String key) {
        return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;
    }

    private String publicKeyFromPublicUrl(String publicUrl) {
        if (publicUrl == null || publicUrl.isBlank()) {
            return null;
        }
        String baseUrl = getPublicFileUrl("");
        if (!publicUrl.startsWith(baseUrl)) {
            return null;
        }
        return publicUrl.substring(baseUrl.length());
    }

    private void createAwsObject(MultipartFile multipartFile, String key) throws IOException {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(multipartFile.getContentType())
                .build();

        s3Client.putObject(
                putObjectRequest,
                RequestBody.fromInputStream(multipartFile.getInputStream(), multipartFile.getSize())
        );
    }

    private void validateImageFile(MultipartFile file) {
        if (!ImageMimeTypes.isAllowed(file.getContentType())) {
            throw ApiExceptions.badRequest(MessageKey.BAD_REQUEST_IMAGE_FILE_MIME_INVALID);
        }
    }

    private String createKey(String id, String keyOfWhat, MultipartFile multipartFile) {
        String contentType = Objects.requireNonNull(multipartFile.getContentType());
        String uniqueSuffix = UUID.randomUUID().toString().replace("-", "");
        return id + keyOfWhat + uniqueSuffix + "." + ImageMimeTypes.fileExtension(contentType);
    }

}
