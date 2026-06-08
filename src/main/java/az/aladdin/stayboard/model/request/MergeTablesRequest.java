package az.aladdin.stayboard.model.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record MergeTablesRequest(
        @NotEmpty @Size(min = 2) List<Long> tableIds,
        @NotNull Long primaryTableId
) {
}
