package az.aladdin.stayboard.service.seating;

import az.aladdin.stayboard.entity.ReservationMainInfo;
import az.aladdin.stayboard.service.seating.support.GuestReservationInfoResolver;
import az.aladdin.stayboard.entity.TableEntity;
import az.aladdin.stayboard.entity.TableOccupancyEntity;
import az.aladdin.stayboard.entity.WaitlistEntryEntity;
import az.aladdin.stayboard.exception.ApiExceptions;
import az.aladdin.stayboard.exception.EntityKey;
import az.aladdin.stayboard.exception.MessageKey;
import az.aladdin.stayboard.mapper.WaitlistEntryMapper;
import az.aladdin.stayboard.model.enums.OccupancySourceType;
import az.aladdin.stayboard.model.enums.WaitlistStatus;
import az.aladdin.stayboard.model.request.CreateWaitlistEntryRequest;
import az.aladdin.stayboard.model.request.ReservationMainInfoRequest;
import az.aladdin.stayboard.model.request.SeatWaitlistEntryRequest;
import az.aladdin.stayboard.model.request.UpdateWaitlistStatusRequest;
import az.aladdin.stayboard.model.request.search.WaitlistSearchCriteria;
import az.aladdin.stayboard.model.response.WaitlistEntryResponse;
import az.aladdin.stayboard.repository.TableOccupancyRepository;
import az.aladdin.stayboard.repository.TableRepository;
import az.aladdin.stayboard.repository.WaitlistEntryRepository;
import az.aladdin.stayboard.security.AuthenticatedUserSupport;
import az.aladdin.stayboard.security.GuestOrderAccess;
import az.aladdin.stayboard.security.GuestWaitlistAccess;
import az.aladdin.stayboard.service.common.UtcDateTimeService;
import az.aladdin.stayboard.service.hotel.HotelAwareService;
import az.aladdin.stayboard.service.hotel.HotelTimeService;
import az.aladdin.stayboard.specification.WaitlistEntrySpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class WaitlistService extends HotelAwareService {

    private static final Set<WaitlistStatus> STAFF_STATUS_TARGETS = EnumSet.of(
            WaitlistStatus.NOTIFIED,
            WaitlistStatus.NO_SHOW
    );

    private final WaitlistEntryRepository waitlistEntryRepository;
    private final TableRepository tableRepository;
    private final TableOccupancyRepository tableOccupancyRepository;
    private final WaitlistEntryMapper waitlistEntryMapper;
    private final TableAvailabilityService tableAvailabilityService;
    private final HotelTimeService hotelTimeService;
    private final UtcDateTimeService utcDateTimeService;
    private final GuestReservationWindowService guestReservationWindowService;
    private final GuestReservationInfoResolver guestReservationInfoResolver;

    @Transactional
    public WaitlistEntryResponse create(CreateWaitlistEntryRequest request) {
        Long hotelId = getCurrentHotelId();
        ensureNoActiveDuplicate(hotelId, request.reservationMainInfo());

        Long reservationId = request.reservationMainInfo() != null
                ? request.reservationMainInfo().reservationId()
                : null;
        LocalDateTime nowHotelLocal = hotelTimeService.nowAtHotel(hotelId);
        guestReservationWindowService.ensureWithinStayWindow(
                reservationId,
                nowHotelLocal,
                nowHotelLocal
        );

        TableEntity preferredTable = resolvePreferredTable(request.preferredTableId(), hotelId);
        ReservationMainInfo reservationMainInfo = guestReservationInfoResolver.resolveForCurrentUser(request.reservationMainInfo());
        WaitlistEntryEntity entity = waitlistEntryMapper.toEntity(
                request,
                hotelId,
                reservationMainInfo,
                preferredTable
        );
        return waitlistEntryMapper.toResponse(waitlistEntryRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public WaitlistEntryResponse get(Long id) {
        return waitlistEntryMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<WaitlistEntryResponse> search(WaitlistSearchCriteria criteria, Pageable pageable) {
        Long hotelId = getCurrentHotelId();
        GuestOrderAccess.Scope guestScope = GuestOrderAccess.currentGuestScope().orElse(null);
        WaitlistSearchCriteria normalizedCriteria = normalizeSearchCriteria(criteria);
        return waitlistEntryRepository.findAll(
                        WaitlistEntrySpecification.withCriteria(hotelId, normalizedCriteria, guestScope),
                        pageable
                )
                .map(waitlistEntryMapper::toResponse);
    }

    @Transactional
    public WaitlistEntryResponse updateStatus(Long id, UpdateWaitlistStatusRequest request) {
        ensureStaffOnly();
        WaitlistEntryEntity entity = getEntityOrThrow(id);
        ensureActiveEntry(entity);

        if (!STAFF_STATUS_TARGETS.contains(request.status())) {
            throw ApiExceptions.badRequest(MessageKey.BAD_REQUEST_INVALID_WAITLIST_STATUS_TRANSITION);
        }
        if (request.status() == WaitlistStatus.NOTIFIED && entity.getStatus() != WaitlistStatus.WAITING) {
            throw ApiExceptions.badRequest(MessageKey.BAD_REQUEST_INVALID_WAITLIST_STATUS_TRANSITION);
        }
        if (request.status() == WaitlistStatus.NO_SHOW && entity.getStatus() != WaitlistStatus.NOTIFIED) {
            throw ApiExceptions.badRequest(MessageKey.BAD_REQUEST_INVALID_WAITLIST_STATUS_TRANSITION);
        }

        entity.setStatus(request.status());
        return waitlistEntryMapper.toResponse(waitlistEntryRepository.save(entity));
    }

    @Transactional
    public WaitlistEntryResponse seat(Long id, SeatWaitlistEntryRequest request) {
        ensureStaffOnly();
        WaitlistEntryEntity entity = getEntityOrThrow(id);
        ensureActiveEntry(entity);

        Long hotelId = getCurrentHotelId();
        TableEntity table = tableRepository.findByIdAndHotelId(request.tableId(), hotelId)
                .orElseThrow(() -> ApiExceptions.notFound(EntityKey.TABLE));

        LocalDateTime startUtc = utcDateTimeService.now();
        int durationMinutes = request.durationMinutes() != null && request.durationMinutes() > 0
                ? request.durationMinutes()
                : TableAvailabilityService.DEFAULT_DINING_DURATION_MINUTES;
        LocalDateTime endUtc = startUtc.plusMinutes(durationMinutes);

        if (!tableAvailabilityService.isTableReservable(hotelId, table, startUtc, endUtc, entity.getPartySize())) {
            throw ApiExceptions.conflict(MessageKey.CONFLICT_TABLE_NOT_AVAILABLE);
        }

        TableOccupancyEntity occupancy = TableOccupancyEntity.builder()
                .hotelId(hotelId)
                .sourceType(OccupancySourceType.OCCUPIED)
                .restaurantTable(table)
                .reservationMainInfo(entity.getReservationMainInfo())
                .startDateTime(startUtc)
                .endDateTime(endUtc)
                .build();
        tableOccupancyRepository.save(occupancy);

        entity.setStatus(WaitlistStatus.SEATED);
        entity.setSeatedTable(table);
        return waitlistEntryMapper.toResponse(waitlistEntryRepository.save(entity));
    }

    @Transactional
    public void cancel(Long id) {
        WaitlistEntryEntity entity = getEntityOrThrow(id);
        if (AuthenticatedUserSupport.isGuest()) {
            GuestWaitlistAccess.ensureGuestCanCancel(entity);
        }
        ensureActiveEntry(entity);
        entity.setStatus(WaitlistStatus.CANCELLED);
        waitlistEntryRepository.save(entity);
    }

    private WaitlistEntryEntity getEntityOrThrow(Long id) {
        Long hotelId = getCurrentHotelId();
        WaitlistEntryEntity entity = requireEntity(
                waitlistEntryRepository.findByIdAndHotelId(id, hotelId),
                EntityKey.WAITLIST_ENTRY
        );
        if (AuthenticatedUserSupport.isGuest()) {
            GuestOrderAccess.Scope scope = GuestOrderAccess.currentGuestScope().orElse(null);
            if (scope != null && !GuestWaitlistAccess.ownsEntry(entity, scope)) {
                throw ApiExceptions.notFound(EntityKey.WAITLIST_ENTRY);
            }
        }
        return entity;
    }

    private void ensureNoActiveDuplicate(Long hotelId, ReservationMainInfoRequest reservationMainInfo) {
        if (!AuthenticatedUserSupport.isGuest()) {
            return;
        }
        GuestOrderAccess.Scope scope = GuestOrderAccess.currentGuestScope().orElse(null);
        if (scope == null) {
            return;
        }
        if (scope.guestUserId() != null
                && waitlistEntryRepository.existsByHotelIdAndStatusInAndReservationMainInfo_GuestInformation_GuestUserId(
                hotelId,
                WaitlistSearchCriteria.ACTIVE_STATUSES,
                scope.guestUserId()
        )) {
            throw ApiExceptions.conflict(MessageKey.CONFLICT_WAITLIST_ALREADY_ACTIVE);
        }
        if (scope.guestEmail() != null
                && waitlistEntryRepository.existsByHotelIdAndStatusInAndReservationMainInfo_GuestInformation_GuestEmailIgnoreCase(
                hotelId,
                WaitlistSearchCriteria.ACTIVE_STATUSES,
                scope.guestEmail()
        )) {
            throw ApiExceptions.conflict(MessageKey.CONFLICT_WAITLIST_ALREADY_ACTIVE);
        }
    }

    private TableEntity resolvePreferredTable(Long preferredTableId, Long hotelId) {
        if (preferredTableId == null) {
            return null;
        }
        return tableRepository.findByIdAndHotelId(preferredTableId, hotelId)
                .orElseThrow(() -> ApiExceptions.notFound(EntityKey.TABLE));
    }

    private WaitlistSearchCriteria normalizeSearchCriteria(WaitlistSearchCriteria criteria) {
        if (criteria == null) {
            if (AuthenticatedUserSupport.isGuest()) {
                return new WaitlistSearchCriteria(null, true, true);
            }
            return new WaitlistSearchCriteria(null, true, null);
        }
        Boolean activeOnly = criteria.activeOnly() != null ? criteria.activeOnly() : true;
        Boolean mineOnly = criteria.mineOnly();
        if (AuthenticatedUserSupport.isGuest() && !Boolean.TRUE.equals(mineOnly)) {
            mineOnly = true;
        }
        return new WaitlistSearchCriteria(criteria.status(), activeOnly, mineOnly);
    }

    private void ensureActiveEntry(WaitlistEntryEntity entity) {
        if (!WaitlistSearchCriteria.ACTIVE_STATUSES.contains(entity.getStatus())) {
            throw ApiExceptions.badRequest(MessageKey.BAD_REQUEST_WAITLIST_ENTRY_NOT_ACTIVE);
        }
    }

    private void ensureStaffOnly() {
        if (AuthenticatedUserSupport.isGuest()) {
            throw ApiExceptions.forbidden(MessageKey.FORBIDDEN_GUEST_WAITLIST_NOT_ALLOWED);
        }
    }
}
