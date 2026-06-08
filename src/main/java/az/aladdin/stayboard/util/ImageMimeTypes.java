package az.aladdin.stayboard.util;

import java.util.Map;

public final class ImageMimeTypes {

    private static final Map<String, String> EXTENSION_BY_MIME = Map.ofEntries(
            Map.entry("image/jpeg", "jpg"),
            Map.entry("image/jpg", "jpg"),
            Map.entry("image/png", "png"),
            Map.entry("image/gif", "gif"),
            Map.entry("image/webp", "webp"),
            Map.entry("image/avif", "avif"),
            Map.entry("image/heic", "heic"),
            Map.entry("image/heif", "heif"),
            Map.entry("image/bmp", "bmp"),
            Map.entry("image/tiff", "tiff")
    );

    private ImageMimeTypes() {
    }

    public static boolean isAllowed(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return false;
        }
        return EXTENSION_BY_MIME.containsKey(contentType.toLowerCase());
    }

    public static String fileExtension(String contentType) {
        String extension = EXTENSION_BY_MIME.get(contentType.toLowerCase());
        if (extension == null) {
            throw new IllegalArgumentException("Unsupported image content type: " + contentType);
        }
        return extension;
    }
}
