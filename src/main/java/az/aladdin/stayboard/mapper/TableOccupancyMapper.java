package az.aladdin.stayboard.mapper;

import az.aladdin.stayboard.entity.GuestInformation;
import az.aladdin.stayboard.entity.ReservationMainInfo;
import az.aladdin.stayboard.entity.TableEntity;
import az.aladdin.stayboard.entity.TableOccupancyEntity;
import az.aladdin.stayboard.model.enums.OccupancySourceType;
import az.aladdin.stayboard.model.request.CreateTableOccupancyRequest;
import az.aladdin.stayboard.model.request.GuestInformationRequest;
import az.aladdin.stayboard.model.request.ReservationMainInfoRequest;
import az.aladdin.stayboard.model.response.GuestInformationResponse;
import az.aladdin.stayboard.model.response.ReservationMainInfoResponse;
import az.aladdin.stayboard.model.response.TableOccupancyResponse;
import az.aladdin.stayboard.security.AuthenticatedUserSupport;
import az.aladdin.stayboard.security.GuestOrderAccess;
import az.aladdin.stayboard.security.GuestTableAccess;
import az.aladdin.stayboard.service.hotel.HotelTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TableOccupancyMapper {

    private final HotelTimeService hotelTimeService;

    public TableOccupancyEntity toEntity(
            CreateTableOccupancyRequest request,
            Long hotelId,
            TableEntity table,
            OccupancySourceType sourceType,
            ReservationMainInfo reservationMainInfo,
            java.time.LocalDateTime startUtc,
            java.time.LocalDateTime endUtc
    ) {
        return TableOccupancyEntity.builder()
                .hotelId(hotelId)
                .sourceType(sourceType)
                .restaurantTable(table)
                .reservationMainInfo(reservationMainInfo)
                .startDateTime(startUtc)
                .endDateTime(endUtc)
                .build();
    }

    public TableOccupancyResponse toResponse(TableOccupancyEntity entity) {
        Long hotelId = entity.getHotelId();
        boolean ownedByCurrentGuest = GuestTableAccess.currentGuestScope()
                .map(scope -> GuestTableAccess.ownsOccupancy(entity, scope))
                .orElse(false);
        boolean maskGuestDetails = AuthenticatedUserSupport.isGuest() && !ownedByCurrentGuest;

        return new TableOccupancyResponse(
                entity.getId(),
                hotelId,
                entity.getRestaurantTable() != null ? entity.getRestaurantTable().getId() : null,
                entity.getRestaurantTable() != null ? entity.getRestaurantTable().getTableNumber() : null,
                entity.getSourceType(),
                toReservationMainInfoResponse(entity.getReservationMainInfo(), maskGuestDetails),
                hotelTimeService.utcLocalDateTimeToHotelLocal(entity.getStartDateTime(), hotelId),
                hotelTimeService.utcLocalDateTimeToHotelLocal(entity.getEndDateTime(), hotelId),
                hotelTimeService.utcLocalDateTimeToHotelLocal(entity.getCreatedAt(), hotelId),
                entity.getCreatedBy(),
                ownedByCurrentGuest
        );
    }

    public ReservationMainInfo toReservationMainInfo(ReservationMainInfoRequest request) {
        if (request == null) {
            return null;
        }
        return new ReservationMainInfo(
                request.reservationId(),
                request.confirmationNumber(),
                request.roomNumber(),
                toGuestInformation(request.guestInformation())
        );
    }

    private GuestInformation toGuestInformation(GuestInformationRequest request) {
        if (request == null) {
            return null;
        }
        return new GuestInformation(
                request.guestFirstName(),
                request.guestLastName(),
                request.guestEmail(),
                null
        );
    }

    private ReservationMainInfoResponse toReservationMainInfoResponse(
            ReservationMainInfo reservationMainInfo,
            boolean maskGuestDetails
    ) {
        if (reservationMainInfo == null) {
            return null;
        }
        GuestInformationResponse guestInformation = maskGuestDetails
                ? null
                : toGuestInformationResponse(reservationMainInfo.guestInformation());
        return new ReservationMainInfoResponse(
                reservationMainInfo.reservationId(),
                reservationMainInfo.confirmationNumber(),
                reservationMainInfo.roomNumber(),
                guestInformation
        );
    }

    private GuestInformationResponse toGuestInformationResponse(GuestInformation guestInformation) {
        if (guestInformation == null) {
            return null;
        }
        return new GuestInformationResponse(
                guestInformation.guestFirstName(),
                guestInformation.guestLastName(),
                guestInformation.guestEmail()
        );
    }
}
