package az.aladdin.stayboard.mapper;

import az.aladdin.stayboard.entity.ReservationMainInfo;
import az.aladdin.stayboard.entity.TableEntity;
import az.aladdin.stayboard.entity.TableOccupancyEntity;
import az.aladdin.stayboard.model.enums.OccupancySourceType;
import az.aladdin.stayboard.model.request.CreateTableOccupancyRequest;
import az.aladdin.stayboard.model.response.TableOccupancyResponse;
import az.aladdin.stayboard.security.AuthenticatedUserSupport;
import az.aladdin.stayboard.security.GuestTableAccess;
import az.aladdin.stayboard.service.hotel.HotelTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TableOccupancyMapper {

    private final HotelTimeService hotelTimeService;
    private final ReservationMainInfoMapper reservationMainInfoMapper;

    public TableOccupancyEntity toEntity(
            CreateTableOccupancyRequest request,
            Long hotelId,
            TableEntity table,
            OccupancySourceType sourceType,
            ReservationMainInfo reservationMainInfo,
            LocalDateTime startUtc,
            LocalDateTime endUtc
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
                reservationMainInfoMapper.toResponse(entity.getReservationMainInfo(), maskGuestDetails),
                hotelTimeService.utcLocalDateTimeToHotelLocal(entity.getStartDateTime(), hotelId),
                hotelTimeService.utcLocalDateTimeToHotelLocal(entity.getEndDateTime(), hotelId),
                hotelTimeService.utcLocalDateTimeToHotelLocal(entity.getCreatedAt(), hotelId),
                entity.getCreatedBy(),
                ownedByCurrentGuest
        );
    }
}
