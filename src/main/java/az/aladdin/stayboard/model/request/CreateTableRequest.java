package az.aladdin.stayboard.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CreateTableRequest(
        @NotBlank String tableNumber,
        @Positive int capacity,
        @Positive int maxCapacity,
        Boolean mergeable,
        List<String> amenities
) {
}
