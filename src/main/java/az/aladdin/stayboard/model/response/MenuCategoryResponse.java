package az.aladdin.stayboard.model.response;

import java.time.LocalDateTime;
import java.util.Set;

public record MenuCategoryResponse(
        Long id,
        Long hotelId,
        String categoryName,
        String description,
        Set<String> photoUrls,
        String mainImageUrl,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime updatedAt,
        String updatedBy
) {
}
