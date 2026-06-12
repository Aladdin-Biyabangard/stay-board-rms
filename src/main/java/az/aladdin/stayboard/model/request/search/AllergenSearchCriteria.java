package az.aladdin.stayboard.model.request.search;

public record AllergenSearchCriteria(
        String allergenName,
        Boolean active
) {
}
