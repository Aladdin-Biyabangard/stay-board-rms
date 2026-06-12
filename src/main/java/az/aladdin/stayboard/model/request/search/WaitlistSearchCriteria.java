package az.aladdin.stayboard.model.request.search;

import az.aladdin.stayboard.model.enums.WaitlistStatus;

import java.util.List;

public record WaitlistSearchCriteria(
        WaitlistStatus status,
        Boolean activeOnly,
        Boolean mineOnly
) {

    public static final List<WaitlistStatus> ACTIVE_STATUSES = List.of(
            WaitlistStatus.WAITING,
            WaitlistStatus.NOTIFIED
    );
}
