package az.aladdin.stayboard.service;

import az.aladdin.stayboard.service.hotel.HotelAwareService;
import az.aladdin.stayboard.entity.GuestInformation;
import az.aladdin.stayboard.entity.ReservationMainInfo;
import az.aladdin.stayboard.entity.TableEntity;
import az.aladdin.stayboard.entity.TableOccupancyEntity;
import az.aladdin.stayboard.exception.ApiExceptions;
import az.aladdin.stayboard.exception.EntityKey;
import az.aladdin.stayboard.exception.MessageKey;
import az.aladdin.stayboard.mapper.TableOccupancyMapper;
import az.aladdin.stayboard.model.enums.OccupancySourceType;
import az.aladdin.stayboard.model.request.CreateTableOccupancyRequest;
import az.aladdin.stayboard.model.request.ReservationMainInfoRequest;
import az.aladdin.stayboard.model.request.search.TableOccupancySearchCriteria;
import az.aladdin.stayboard.model.response.TableOccupancyResponse;
import az.aladdin.stayboard.repository.TableOccupancyRepository;
import az.aladdin.stayboard.repository.TableRepository;
import az.aladdin.stayboard.security.AuthenticatedUserSupport;
import az.aladdin.stayboard.security.GuestOrderAccess;
import az.aladdin.stayboard.security.GuestTableAccess;
import az.aladdin.stayboard.service.hotel.HotelTimeService;
import az.aladdin.stayboard.specification.TableOccupancySpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TableOccupancyService extends HotelAwareService {

    private final TableOccupancyRepository tableOccupancyRepository;
    private final TableRepository tableRepository;
    private final TableOccupancyMapper tableOccupancyMapper;
    private final TableAvailabilityService tableAvailabilityService;
    private final HotelTimeService hotelTimeService;
    private final UtcDateTimeService utcDateTimeService;
    private final GuestReservationWindowService guestReservationWindowService;

    @Transactional
    public TableOccupancyResponse create(CreateTableOccupancyRequest request) {
        Long hotelId = getCurrentHotelId();
        OccupancySourceType sourceType = resolveSourceType(request.sourceType());
        GuestTableAccess.ensureGuestCanReserve();

        if (request.endDateTime() == null || request.startDateTime() == null
                || !request.endDateTime().isAfter(request.startDateTime())) {
            throw ApiExceptions.badRequest(MessageKey.BAD_REQUEST_INVALID_TABLE_OCCUPANCY_WINDOW);
        }

        LocalDateTime startUtc = hotelTimeService.hotelLocalDateTimeToUtc(request.startDateTime(), hotelId);
        LocalDateTime endUtc = hotelTimeService.hotelLocalDateTimeToUtc(request.endDateTime(), hotelId);
        if (!endUtc.isAfter(utcDateTimeService.now()) && AuthenticatedUserSupport.isGuest()) {
            throw ApiExceptions.badRequest(MessageKey.BAD_REQUEST_TABLE_RESERVATION_IN_PAST);
        }

        Long reservationId = request.reservationMainInfo() != null
                ? request.reservationMainInfo().reservationId()
                : null;
        guestReservationWindowService.ensureWithinStayWindow(
                reservationId,
                request.startDateTime(),
                request.endDateTime()
        );

        TableEntity table = tableRepository.findByIdAndHotelId(request.tableId(), hotelId)
                .orElseThrow(() -> ApiExceptions.notFound(EntityKey.TABLE));

        if (!tableAvailabilityService.isTableReservable(hotelId, table, startUtc, endUtc, request.partySize())) {
            throw ApiExceptions.conflict(MessageKey.CONFLICT_TABLE_NOT_AVAILABLE);
        }

        ReservationMainInfo reservationMainInfo = resolveReservationMainInfo(request.reservationMainInfo());
        TableOccupancyEntity entity = tableOccupancyMapper.toEntity(
                request,
                hotelId,
                table,
                sourceType,
                reservationMainInfo,
                startUtc,
                endUtc
        );
        return tableOccupancyMapper.toResponse(tableOccupancyRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public TableOccupancyResponse get(Long id) {
        return tableOccupancyMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<TableOccupancyResponse> search(TableOccupancySearchCriteria criteria, Pageable pageable) {
        Long hotelId = getCurrentHotelId();
        GuestOrderAccess.Scope guestScope = GuestOrderAccess.currentGuestScope().orElse(null);
        TableOccupancySearchCriteria normalizedCriteria = normalizeSearchCriteria(criteria, hotelId);
        return tableOccupancyRepository.findAll(
                        TableOccupancySpecification.withCriteria(hotelId, normalizedCriteria, guestScope),
                        pageable
                )
                .map(tableOccupancyMapper::toResponse);
    }

    @Transactional
    public void delete(Long id) {
        TableOccupancyEntity entity = getEntityOrThrow(id);
        if (AuthenticatedUserSupport.isGuest()) {
            GuestTableAccess.ensureGuestCanCancel(entity);
            if (entity.getSourceType() != OccupancySourceType.RESERVED) {
                throw ApiExceptions.forbidden(MessageKey.FORBIDDEN_GUEST_TABLE_RESERVATION_NOT_ALLOWED);
            }
        }
        tableOccupancyRepository.delete(entity);
    }

    private TableOccupancyEntity getEntityOrThrow(Long id) {
        Long hotelId = getCurrentHotelId();
        TableOccupancyEntity entity = tableOccupancyRepository.findByIdAndHotelId(id, hotelId)
                .orElseThrow(() -> ApiExceptions.notFound(EntityKey.TABLE_OCCUPANCY));
        if (AuthenticatedUserSupport.isGuest()) {
            GuestOrderAccess.Scope scope = GuestOrderAccess.currentGuestScope().orElse(null);
            if (scope != null && !GuestTableAccess.ownsOccupancy(entity, scope)) {
                throw ApiExceptions.notFound(EntityKey.TABLE_OCCUPANCY);
            }
        }
        return entity;
    }

    private OccupancySourceType resolveSourceType(OccupancySourceType requested) {
        if (AuthenticatedUserSupport.isGuest()) {
            return OccupancySourceType.RESERVED;
        }
        if (requested == null) {
            throw ApiExceptions.badRequest(MessageKey.BAD_REQUEST_TABLE_OCCUPANCY_SOURCE_REQUIRED);
        }
        return requested;
    }

    private ReservationMainInfo resolveReservationMainInfo(ReservationMainInfoRequest request) {
        ReservationMainInfo reservationMainInfo = tableOccupancyMapper.toReservationMainInfo(request);
        if (!AuthenticatedUserSupport.isGuest()) {
            return reservationMainInfo;
        }

        GuestInformation guestInformation = reservationMainInfo != null
                ? reservationMainInfo.guestInformation()
                : null;
        if (guestInformation == null) {
            guestInformation = new GuestInformation(null, null, null, null);
        }
        guestInformation = GuestTableAccess.attachGuestUserId(
                guestInformation,
                AuthenticatedUserSupport.requirePrincipal().getUserId()
        );
        if (guestInformation.guestEmail() == null) {
            guestInformation = new GuestInformation(
                    guestInformation.guestFirstName(),
                    guestInformation.guestLastName(),
                    GuestOrderAccess.comparableGuestEmail(AuthenticatedUserSupport.requirePrincipal().getEmail()),
                    guestInformation.guestUserId()
            );
        }

        if (reservationMainInfo == null) {
            return new ReservationMainInfo(null, null, null, guestInformation);
        }
        return new ReservationMainInfo(
                reservationMainInfo.reservationId(),
                reservationMainInfo.confirmationNumber(),
                reservationMainInfo.roomNumber(),
                guestInformation
        );
    }

    private TableOccupancySearchCriteria normalizeSearchCriteria(TableOccupancySearchCriteria criteria, Long hotelId) {
        if (criteria == null) {
            return null;
        }
        return new TableOccupancySearchCriteria(
                criteria.tableId(),
                criteria.sourceType(),
                hotelTimeService.hotelLocalDateTimeToUtc(criteria.from(), hotelId),
                hotelTimeService.hotelLocalDateTimeToUtc(criteria.to(), hotelId),
                criteria.mineOnly()
        );
    }
}
