package az.aladdin.stayboard.model.request.search;

public record RecipeSearchCriteria(
        Long menuItemId,
        Long inventoryItemId
) {
}
