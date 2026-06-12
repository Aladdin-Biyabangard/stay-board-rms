package az.aladdin.stayboard.model.response;

import java.time.LocalDateTime;

public record AllergenResponse(
        Long id,
        Long hotelId,
        String allergenName,
        String description,
        boolean active,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime updatedAt,
        String updatedBy
) {
}
