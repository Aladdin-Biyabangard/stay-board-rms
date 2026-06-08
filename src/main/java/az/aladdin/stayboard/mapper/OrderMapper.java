package az.aladdin.stayboard.mapper;

import az.aladdin.stayboard.entity.GuestInformation;
import az.aladdin.stayboard.entity.OrderEntity;
import az.aladdin.stayboard.entity.TableEntity;
import az.aladdin.stayboard.model.request.CreateOrderRequest;
import az.aladdin.stayboard.model.request.GuestInformationRequest;
import az.aladdin.stayboard.model.request.PatchOrderRequest;
import az.aladdin.stayboard.model.request.UpdateOrderRequest;
import az.aladdin.stayboard.model.response.GuestInformationResponse;
import az.aladdin.stayboard.model.response.OrderResponse;
import az.aladdin.stayboard.service.hotel.HotelTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderMapper {

    private final HotelTimeService hotelTimeService;

    public OrderEntity toEntity(CreateOrderRequest request, Long hotelId, TableEntity tableEntity) {
        return OrderEntity.builder()
                .hotelId(hotelId)
                .orderNumber(request.orderNumber())
                .guestInformation(toGuestInformation(request.guestInformation()))
                .tableEntity(tableEntity)
                .roomNumber(request.roomNumber())
                .totalAmount(request.totalAmount())
                .orderStatus(request.orderStatus())
                .build();
    }

    public void updateEntity(OrderEntity entity, UpdateOrderRequest request, TableEntity tableEntity) {
        entity.setOrderNumber(request.orderNumber());
        entity.setGuestInformation(toGuestInformation(request.guestInformation()));
        entity.setTableEntity(tableEntity);
        entity.setRoomNumber(request.roomNumber());
        entity.setTotalAmount(request.totalAmount());
        entity.setOrderStatus(request.orderStatus());
    }

    public void patchEntity(OrderEntity entity, PatchOrderRequest request, TableEntity tableEntity) {
        if (request.orderNumber() != null) {
            entity.setOrderNumber(request.orderNumber());
        }
        if (request.guestInformation() != null) {
            entity.setGuestInformation(toGuestInformation(request.guestInformation()));
        }
        if (request.tableId() != null || tableEntity != null) {
            entity.setTableEntity(tableEntity);
        }
        if (request.roomNumber() != null) {
            entity.setRoomNumber(request.roomNumber());
        }
        if (request.totalAmount() != null) {
            entity.setTotalAmount(request.totalAmount());
        }
        if (request.orderStatus() != null) {
            entity.setOrderStatus(request.orderStatus());
        }
    }

    public OrderResponse toResponse(OrderEntity entity) {
        Long hotelId = entity.getHotelId();
        return new OrderResponse(
                entity.getId(),
                hotelId,
                entity.getOrderNumber(),
                toGuestInformationResponse(entity.getGuestInformation()),
                entity.getTableEntity() != null ? entity.getTableEntity().getId() : null,
                entity.getTableEntity() != null ? entity.getTableEntity().getTableNumber() : null,
                entity.getRoomNumber(),
                entity.getTotalAmount(),
                entity.getOrderStatus(),
                hotelTimeService.utcLocalDateTimeToHotelLocal(entity.getCreatedAt(), hotelId),
                entity.getCreatedBy(),
                hotelTimeService.utcLocalDateTimeToHotelLocal(entity.getUpdatedAt(), hotelId),
                entity.getUpdatedBy(),
                hotelTimeService.resolveHotelTimezone(hotelId)
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
