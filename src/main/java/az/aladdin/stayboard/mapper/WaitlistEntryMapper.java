package az.aladdin.stayboard.mapper;

import az.aladdin.stayboard.entity.TableEntity;
import az.aladdin.stayboard.entity.WaitlistEntryEntity;
import az.aladdin.stayboard.model.enums.WaitlistStatus;
import az.aladdin.stayboard.model.request.CreateWaitlistEntryRequest;
import az.aladdin.stayboard.model.request.search.WaitlistSearchCriteria;
import az.aladdin.stayboard.model.response.WaitlistEntryResponse;
import az.aladdin.stayboard.repository.WaitlistEntryRepository;
import az.aladdin.stayboard.security.AuthenticatedUserSupport;
import az.aladdin.stayboard.security.GuestWaitlistAccess;
import az.aladdin.stayboard.entity.ReservationMainInfo;
import az.aladdin.stayboard.service.hotel.HotelTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WaitlistEntryMapper {

    private final HotelTimeService hotelTimeService;
    private final WaitlistEntryRepository waitlistEntryRepository;
    private final ReservationMainInfoMapper reservationMainInfoMapper;

    public WaitlistEntryEntity toEntity(
            CreateWaitlistEntryRequest request,
            Long hotelId,
            ReservationMainInfo reservationMainInfo,
            TableEntity preferredTable
    ) {
        return WaitlistEntryEntity.builder()
                .hotelId(hotelId)
                .partySize(request.partySize())
                .status(WaitlistStatus.WAITING)
                .reservationMainInfo(reservationMainInfo)
                .notes(request.notes())
                .estimatedWaitMinutes(request.estimatedWaitMinutes())
                .preferredTable(preferredTable)
                .build();
    }

    public WaitlistEntryResponse toResponse(WaitlistEntryEntity entity) {
        Long hotelId = entity.getHotelId();
        boolean ownedByCurrentGuest = GuestWaitlistAccess.currentGuestScope()
                .map(scope -> GuestWaitlistAccess.ownsEntry(entity, scope))
                .orElse(false);
        boolean maskGuestDetails = AuthenticatedUserSupport.isGuest() && !ownedByCurrentGuest;

        int queuePosition = 0;
        if (WaitlistSearchCriteria.ACTIVE_STATUSES.contains(entity.getStatus()) && entity.getCreatedAt() != null) {
            queuePosition = (int) waitlistEntryRepository.countByHotelIdAndStatusInAndCreatedAtLessThanEqual(
                    hotelId,
                    WaitlistSearchCriteria.ACTIVE_STATUSES,
                    entity.getCreatedAt()
            );
        }

        return new WaitlistEntryResponse(
                entity.getId(),
                hotelId,
                entity.getPartySize(),
                entity.getStatus(),
                reservationMainInfoMapper.toResponse(entity.getReservationMainInfo(), maskGuestDetails),
                entity.getNotes(),
                entity.getEstimatedWaitMinutes(),
                entity.getPreferredTable() != null ? entity.getPreferredTable().getId() : null,
                entity.getPreferredTable() != null ? entity.getPreferredTable().getTableNumber() : null,
                entity.getSeatedTable() != null ? entity.getSeatedTable().getId() : null,
                entity.getSeatedTable() != null ? entity.getSeatedTable().getTableNumber() : null,
                queuePosition,
                hotelTimeService.utcLocalDateTimeToHotelLocal(entity.getCreatedAt(), hotelId),
                entity.getCreatedBy(),
                ownedByCurrentGuest
        );
    }
}
