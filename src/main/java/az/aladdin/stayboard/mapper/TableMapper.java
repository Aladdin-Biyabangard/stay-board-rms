package az.aladdin.stayboard.mapper;

import az.aladdin.stayboard.entity.TableEntity;
import az.aladdin.stayboard.model.request.CreateTableRequest;
import az.aladdin.stayboard.model.request.PatchTableRequest;
import az.aladdin.stayboard.model.request.UpdateTableRequest;
import az.aladdin.stayboard.model.response.TableResponse;
import az.aladdin.stayboard.service.hotel.HotelTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TableMapper {

    private final HotelTimeService hotelTimeService;

    public TableEntity toEntity(CreateTableRequest request, Long hotelId) {
        return TableEntity.builder()
                .hotelId(hotelId)
                .tableNumber(request.tableNumber())
                .capacity(request.capacity())
                .maxCapacity(request.maxCapacity())
                .mergeable(request.mergeable() != null ? request.mergeable() : true)
                .amenities(copyAmenities(request.amenities()))
                .build();
    }

    public void updateEntity(TableEntity entity, UpdateTableRequest request) {
        entity.setTableNumber(request.tableNumber());
        entity.setCapacity(request.capacity());
        entity.setMaxCapacity(request.maxCapacity());
        entity.setMergeable(request.mergeable());
        entity.setAmenities(copyAmenities(request.amenities()));
    }

    public void patchEntity(TableEntity entity, PatchTableRequest request) {
        if (request.tableNumber() != null) {
            entity.setTableNumber(request.tableNumber());
        }
        if (request.capacity() != null) {
            entity.setCapacity(request.capacity());
        }
        if (request.maxCapacity() != null) {
            entity.setMaxCapacity(request.maxCapacity());
        }
        if (request.mergeable() != null) {
            entity.setMergeable(request.mergeable());
        }
        if (request.amenities() != null) {
            entity.setAmenities(copyAmenities(request.amenities()));
        }
    }

    public TableResponse toResponse(TableEntity entity) {
        Long hotelId = entity.getHotelId();
        return new TableResponse(
                entity.getId(),
                hotelId,
                entity.getTableNumber(),
                entity.getCapacity(),
                entity.getMaxCapacity(),
                entity.isMergeable(),
                entity.getMergeGroupId(),
                entity.getPrimaryTableId(),
                entity.getAmenities(),
                hotelTimeService.utcLocalDateTimeToHotelLocal(entity.getCreatedAt(), hotelId),
                entity.getCreatedBy(),
                hotelTimeService.utcLocalDateTimeToHotelLocal(entity.getUpdatedAt(), hotelId),
                entity.getUpdatedBy()
        );
    }

    private List<String> copyAmenities(List<String> amenities) {
        return amenities == null ? new ArrayList<>() : new ArrayList<>(amenities);
    }
}
