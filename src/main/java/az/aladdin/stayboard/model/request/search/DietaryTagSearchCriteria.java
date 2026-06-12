package az.aladdin.stayboard.model.request.search;

public record DietaryTagSearchCriteria(
        String tagName,
        Boolean active
) {
}
