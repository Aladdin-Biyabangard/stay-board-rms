package az.aladdin.stayboard.model.request.search;

public record ModifierGroupSearchCriteria(
        String groupName,
        Boolean active
) {
}
