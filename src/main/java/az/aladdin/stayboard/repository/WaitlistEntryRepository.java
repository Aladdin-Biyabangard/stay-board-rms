package az.aladdin.stayboard.repository;

import az.aladdin.stayboard.entity.WaitlistEntryEntity;
import az.aladdin.stayboard.model.enums.WaitlistStatus;
import az.aladdin.stayboard.repository.base.HotelAwareSpecificationRepository;

import java.time.LocalDateTime;
import java.util.Collection;

public interface WaitlistEntryRepository extends HotelAwareSpecificationRepository<WaitlistEntryEntity, Long> {

    long countByHotelIdAndStatusIn(
            Long hotelId,
            Collection<WaitlistStatus> statuses
    );

    long countByHotelIdAndStatusInAndCreatedAtLessThanEqual(
            Long hotelId,
            Collection<WaitlistStatus> statuses,
            LocalDateTime createdAt
    );

    boolean existsByHotelIdAndStatusInAndReservationMainInfo_GuestInformation_GuestUserId(
            Long hotelId,
            Collection<WaitlistStatus> statuses,
            Long guestUserId
    );

    boolean existsByHotelIdAndStatusInAndReservationMainInfo_GuestInformation_GuestEmailIgnoreCase(
            Long hotelId,
            Collection<WaitlistStatus> statuses,
            String guestEmail
    );
}
