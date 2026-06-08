package az.aladdin.stayboard.model.request;

public record PatchMenuCategoryRequest(
        String categoryName,
        String description
) {
}
