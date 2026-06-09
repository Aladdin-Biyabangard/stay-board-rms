package az.aladdin.stayboard.service;

import az.aladdin.stayboard.entity.MenuItemEntity;
import az.aladdin.stayboard.entity.OrderEntity;
import az.aladdin.stayboard.entity.OrderItemEntity;
import az.aladdin.stayboard.exception.ApiExceptions;
import az.aladdin.stayboard.exception.EntityKey;
import az.aladdin.stayboard.mapper.OrderItemMapper;
import az.aladdin.stayboard.model.request.CreateOrderItemLineRequest;
import az.aladdin.stayboard.model.request.CreateOrderItemRequest;
import az.aladdin.stayboard.model.request.PatchOrderItemRequest;
import az.aladdin.stayboard.model.request.UpdateOrderItemRequest;
import az.aladdin.stayboard.model.request.search.OrderItemSearchCriteria;
import az.aladdin.stayboard.model.response.OrderItemResponse;
import az.aladdin.stayboard.repository.MenuItemRepository;
import az.aladdin.stayboard.repository.OrderItemRepository;
import az.aladdin.stayboard.repository.OrderRepository;
import az.aladdin.stayboard.security.GuestOrderAccess;
import az.aladdin.stayboard.service.hotel.HotelAwareService;
import az.aladdin.stayboard.specification.OrderItemSpecification;
import az.aladdin.stayboard.util.OrderItemPricingSupport;
import az.aladdin.stayboard.util.OrderItemQuantitySupport;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class OrderItemService extends HotelAwareService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrderItemMapper orderItemMapper;
    private final UtcDateTimeService utcDateTimeService;
    private final OrderItemFolioSyncService orderItemFolioSyncService;
    private final OrderStatusSyncService orderStatusSyncService;

    @Transactional
    public OrderItemResponse create(CreateOrderItemRequest request) {
        Long hotelId = getCurrentHotelId();
        OrderEntity order = fetchOrderEntity(request.orderId(), hotelId);
        GuestOrderAccess.ensureGuestCanModifyOrder(order, utcDateTimeService.now());
        MenuItemEntity menuItem = fetchMenuItemEntity(request.menuItemId(), hotelId);
        OrderItemQuantitySupport.validate(menuItem, request.quantity(), request.weightQuantity());
        OrderItemEntity entity = orderItemMapper.toEntity(request, hotelId, order, menuItem);
        OrderItemPricingSupport.applyPricing(entity, menuItem);
        entity = orderItemRepository.save(entity);
        syncOrderTotal(order);
        orderItemFolioSyncService.postCharge(entity);
        orderStatusSyncService.syncFromOrderItems(order);
        return orderItemMapper.toResponse(entity);
    }

    /**
     * Creates multiple order items for a newly created order in one transaction.
     * Folio charges are posted sequentially to avoid PMS folio deadlocks on the same room.
     */
    public void createLinesForOrder(OrderEntity order, List<CreateOrderItemLineRequest> lines) {
        if (lines == null || lines.isEmpty()) {
            return;
        }
        Long hotelId = order.getHotelId();
        GuestOrderAccess.ensureGuestCanModifyOrder(order, utcDateTimeService.now());

        for (CreateOrderItemLineRequest line : lines) {
            MenuItemEntity menuItem = fetchMenuItemEntity(line.menuItemId(), hotelId);
            OrderItemQuantitySupport.validate(menuItem, line.quantity(), line.weightQuantity());
            OrderItemEntity entity = orderItemMapper.toEntityFromLine(line, hotelId, order, menuItem);
            OrderItemPricingSupport.applyPricing(entity, menuItem);
            entity = orderItemRepository.save(entity);
            orderItemFolioSyncService.postCharge(entity);
        }

        syncOrderTotal(order);
        orderStatusSyncService.syncFromOrderItems(order);
    }

    @Transactional
    public OrderItemResponse update(Long id, UpdateOrderItemRequest request) {
        return saveModifiedOrderItem(id, context -> {
            OrderEntity order = fetchOrderEntity(request.orderId(), context.hotelId());
            GuestOrderAccess.ensureGuestCanModifyOrder(order, utcDateTimeService.now());
            MenuItemEntity menuItem = fetchMenuItemEntity(request.menuItemId(), context.hotelId());
            OrderItemQuantitySupport.validate(menuItem, request.quantity(), request.weightQuantity());
            orderItemMapper.updateEntity(context.entity(), request, order, menuItem);
            OrderItemPricingSupport.applyPricing(context.entity(), menuItem);
            return order;
        });
    }

    @Transactional
    public OrderItemResponse patch(Long id, PatchOrderItemRequest request) {
        return saveModifiedOrderItem(id, context -> {
            OrderEntity order = request.orderId() != null
                    ? fetchOrderEntity(request.orderId(), context.hotelId())
                    : null;
            if (order != null) {
                GuestOrderAccess.ensureGuestCanModifyOrder(order, utcDateTimeService.now());
            }
            MenuItemEntity menuItem = request.menuItemId() != null
                    ? fetchMenuItemEntity(request.menuItemId(), context.hotelId())
                    : null;
            orderItemMapper.patchEntity(context.entity(), request, order, menuItem);
            MenuItemEntity pricingMenuItem = menuItem != null ? menuItem : context.entity().getMenuItem();
            if (pricingMenuItem != null) {
                OrderItemQuantitySupport.validate(
                        pricingMenuItem,
                        context.entity().getQuantity(),
                        context.entity().getWeightQuantity()
                );
                OrderItemPricingSupport.applyPricing(context.entity(), pricingMenuItem);
            }
            return order != null ? order : context.entity().getOrder();
        });
    }

    @Transactional(readOnly = true)
    public OrderItemResponse get(Long id) {
        return orderItemMapper.toResponse(fetchOrderItemEntity(id));
    }

    @Transactional(readOnly = true)
    public Page<OrderItemResponse> search(OrderItemSearchCriteria criteria, Pageable pageable) {
        Long hotelId = getCurrentHotelId();
        GuestOrderAccess.Scope guestScope = GuestOrderAccess.currentGuestScope().orElse(null);
        return orderItemRepository.findAll(OrderItemSpecification.withCriteria(hotelId, criteria, guestScope), pageable)
                .map(orderItemMapper::toResponse);
    }

    @Transactional
    public void delete(Long id) {
        OrderItemEntity entity = fetchOrderItemEntity(id);
        GuestOrderAccess.ensureGuestCanModifyOrder(entity.getOrder(), utcDateTimeService.now());
        OrderEntity order = entity.getOrder();
        orderItemFolioSyncService.voidCharge(entity);
        orderItemRepository.delete(entity);
        syncOrderTotal(order);
        orderStatusSyncService.syncFromOrderItems(order);
    }

    private OrderItemResponse saveModifiedOrderItem(
            Long id,
            Function<OrderItemMutationContext, OrderEntity> mutator
    ) {
        OrderItemEntity entity = fetchOrderItemEntity(id);
        GuestOrderAccess.ensureGuestCanModifyOrder(entity.getOrder(), utcDateTimeService.now());
        OrderEntity order = mutator.apply(new OrderItemMutationContext(entity, getCurrentHotelId()));
        entity = orderItemRepository.save(entity);
        if (order != null) {
            syncOrderTotal(order);
        }
        orderItemFolioSyncService.updateCharge(entity);
        orderStatusSyncService.syncFromOrderItems(entity.getOrder());
        return orderItemMapper.toResponse(entity);
    }

    private OrderItemEntity fetchOrderItemEntity(Long id) {
        OrderItemEntity entity = orderItemRepository.findByIdAndHotelId(id, getCurrentHotelId())
                .orElseThrow(() -> ApiExceptions.notFound(EntityKey.ORDER_ITEM));
        GuestOrderAccess.ensureGuestCanAccessOrder(entity.getOrder());
        return entity;
    }

    private OrderEntity fetchOrderEntity(Long orderId, Long hotelId) {
        OrderEntity order = orderRepository.findByIdAndHotelId(orderId, hotelId)
                .orElseThrow(() -> ApiExceptions.notFound(EntityKey.ORDER));
        GuestOrderAccess.ensureGuestCanAccessOrder(order);
        return order;
    }

    private MenuItemEntity fetchMenuItemEntity(Long menuItemId, Long hotelId) {
        return menuItemRepository.findByIdAndHotelId(menuItemId, hotelId)
                .orElseThrow(() -> ApiExceptions.notFound(EntityKey.MENU_ITEM));
    }

    private void syncOrderTotal(OrderEntity order) {
        if (order == null) {
            return;
        }
        List<OrderItemEntity> items = orderItemRepository.findAllByOrder_IdAndHotelId(order.getId(), order.getHotelId());
        BigDecimal total = items.stream()
                .map(OrderItemEntity::getGrossAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);
        orderRepository.save(order);
    }

    private record OrderItemMutationContext(OrderItemEntity entity, Long hotelId) {
    }
}
