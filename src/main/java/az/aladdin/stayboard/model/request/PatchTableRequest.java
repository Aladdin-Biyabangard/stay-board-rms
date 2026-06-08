package az.aladdin.stayboard.model.request;

import java.util.List;

public record PatchTableRequest(
        String tableNumber,
        Integer capacity,
        Integer maxCapacity,
        Boolean mergeable,
        List<String> amenities
) {
}
