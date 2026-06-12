package az.aladdin.stayboard.model.request.search;

public record ModifierOptionSearchCriteria(
        Long modifierGroupId,
        String optionName,
        Boolean active
) {
}
