package az.aladdin.stayboard.model.request;

import jakarta.validation.constraints.NotBlank;

public record CreateMenuCategoryRequest(
        @NotBlank String categoryName,
        String description
) {
}
