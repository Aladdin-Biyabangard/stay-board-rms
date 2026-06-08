package az.aladdin.stayboard.model.request.search;

public record TableSearchCriteria(
        String tableNumber,
        Integer minCapacity,
        Integer maxCapacity,
        Boolean mergeable,
        Boolean merged
) {
}
