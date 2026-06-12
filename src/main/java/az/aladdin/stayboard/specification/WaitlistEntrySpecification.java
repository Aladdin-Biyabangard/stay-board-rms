package az.aladdin.stayboard.specification;

import az.aladdin.stayboard.entity.WaitlistEntryEntity;
import az.aladdin.stayboard.model.request.search.WaitlistSearchCriteria;
import az.aladdin.stayboard.security.GuestOrderAccess;
import az.aladdin.stayboard.security.GuestWaitlistAccess;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class WaitlistEntrySpecification {

    private WaitlistEntrySpecification() {
    }

    public static Specification<WaitlistEntryEntity> withCriteria(
            Long hotelId,
            WaitlistSearchCriteria criteria,
            GuestOrderAccess.Scope guestScope
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("hotelId"), hotelId));

            if (criteria != null) {
                if (criteria.status() != null) {
                    predicates.add(cb.equal(root.get("status"), criteria.status()));
                }
                if (criteria.activeOnly() != null && criteria.activeOnly()) {
                    predicates.add(root.get("status").in(WaitlistSearchCriteria.ACTIVE_STATUSES));
                }
                if (Boolean.TRUE.equals(criteria.mineOnly()) && guestScope != null) {
                    predicates.add(GuestWaitlistAccess.ownershipPredicate(cb, root.get("reservationMainInfo"), guestScope));
                }
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
