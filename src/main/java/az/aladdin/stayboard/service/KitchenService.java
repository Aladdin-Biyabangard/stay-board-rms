package az.aladdin.stayboard.service;

import az.aladdin.stayboard.service.hotel.HotelAwareService;
import az.aladdin.stayboard.entity.OrderItemEntity;
import az.aladdin.stayboard.exception.ApiExceptions;
import az.aladdin.stayboard.exception.EntityKey;
import az.aladdin.stayboard.exception.MessageKey;
import az.aladdin.stayboard.mapper.KitchenTicketMapper;
import az.aladdin.stayboard.model.enums.OrderItemStatus;
import az.aladdin.stayboard.model.request.UpdateKitchenTicketStatusRequest;
import az.aladdin.stayboard.model.request.search.KitchenTicketSearchCriteria;
import az.aladdin.stayboard.model.response.KitchenTicketResponse;
import az.aladdin.stayboard.repository.OrderItemRepository;
import az.aladdin.stayboard.specification.KitchenTicketSpecification;
import az.aladdin.stayboard.util.OrderItemStatusTransition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KitchenService extends HotelAwareService {

    private final OrderItemRepository orderItemRepository;
    private final KitchenTicketMapper kitchenTicketMapper;
    private final InventoryConsumptionService inventoryConsumptionService;
    private final OrderStatusSyncService orderStatusSyncService;

    @Transactional(readOnly = true)
    public Page<KitchenTicketResponse> searchTickets(KitchenTicketSearchCriteria criteria, Pageable pageable) {
        Long hotelId = getCurrentHotelId();
        Pageable sortedPageable = pageable.getSort().isSorted()
                ? pageable
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.ASC, "createdAt"));
        return orderItemRepository.findAll(KitchenTicketSpecification.withCriteria(hotelId, criteria), sortedPageable)
                .map(kitchenTicketMapper::toResponse);
    }

    @Transactional
    public KitchenTicketResponse updateStatus(Long orderItemId, UpdateKitchenTicketStatusRequest request) {
        OrderItemEntity entity = getEntityOrThrow(orderItemId);
        OrderItemStatus currentStatus = entity.getOrderItemStatus();
        OrderItemStatus targetStatus = request.orderItemStatus();

        OrderItemStatusTransition.validate(currentStatus, targetStatus);

        if (OrderItemStatusTransition.consumesInventory(currentStatus, targetStatus)) {
            inventoryConsumptionService.consumeForOrderItem(entity);
        }
        if (OrderItemStatusTransition.reversesInventory(currentStatus, targetStatus)) {
            inventoryConsumptionService.reverseForOrderItem(entity.getId());
        }

        entity.setOrderItemStatus(targetStatus);
        entity = orderItemRepository.save(entity);
        orderStatusSyncService.syncFromOrderItems(entity.getOrder());
        return kitchenTicketMapper.toResponse(entity);
    }

    private OrderItemEntity getEntityOrThrow(Long id) {
        Long hotelId = getCurrentHotelId();
        return orderItemRepository.findByIdAndHotelId(id, hotelId)
                .orElseThrow(() -> ApiExceptions.notFound(EntityKey.ORDER_ITEM));
    }
}
