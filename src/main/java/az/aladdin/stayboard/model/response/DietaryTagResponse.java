package az.aladdin.stayboard.model.response;

import java.time.LocalDateTime;

public record DietaryTagResponse(
        Long id,
        Long hotelId,
        String tagName,
        String description,
        boolean active,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime updatedAt,
        String updatedBy
) {
}
