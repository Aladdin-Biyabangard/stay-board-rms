package az.aladdin.stayboard.service;

import az.aladdin.stayboard.context.HotelContextHolder;
import az.aladdin.stayboard.entity.MenuItemEntity;
import az.aladdin.stayboard.entity.OrderEntity;
import az.aladdin.stayboard.entity.OrderItemEntity;
import az.aladdin.stayboard.exception.ApiExceptions;
import az.aladdin.stayboard.exception.EntityKey;
import az.aladdin.stayboard.exception.MessageKey;
import az.aladdin.stayboard.mapper.OrderItemMapper;
import az.aladdin.stayboard.model.request.CreateOrderItemRequest;
import az.aladdin.stayboard.model.request.PatchOrderItemRequest;
import az.aladdin.stayboard.model.request.UpdateOrderItemRequest;
import az.aladdin.stayboard.model.request.search.OrderItemSearchCriteria;
import az.aladdin.stayboard.model.response.OrderItemResponse;
import az.aladdin.stayboard.repository.MenuItemRepository;
import az.aladdin.stayboard.repository.OrderItemRepository;
import az.aladdin.stayboard.repository.OrderRepository;
import az.aladdin.stayboard.security.GuestOrderAccess;
import az.aladdin.stayboard.specification.OrderItemSpecification;
import az.aladdin.stayboard.util.OrderItemQuantitySupport;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrderItemMapper orderItemMapper;
    private final UtcDateTimeService utcDateTimeService;
    private final OrderItemFolioSyncService orderItemFolioSyncService;

    @Transactional
    public OrderItemResponse create(CreateOrderItemRequest request) {
        Long hotelId = HotelContextHolder.getHotelIdOrThrow();
        OrderEntity order = getOrderOrThrow(request.orderId(), hotelId);
        ensureGuestCanModifyOrder(order);
        MenuItemEntity menuItem = getMenuItemOrThrow(request.menuItemId(), hotelId);
        OrderItemQuantitySupport.validate(menuItem, request.quantity(), request.weightQuantity());
        OrderItemEntity entity = orderItemMapper.toEntity(request, hotelId, order, menuItem);
        entity = orderItemRepository.save(entity);
        orderItemFolioSyncService.postCharge(entity);
        return orderItemMapper.toResponse(entity);
    }

    @Transactional
    public OrderItemResponse update(Long id, UpdateOrderItemRequest request) {
        Long hotelId = HotelContextHolder.getHotelIdOrThrow();
        OrderItemEntity entity = getEntityOrThrow(id);
        ensureGuestCanModifyOrder(entity.getOrder());
        OrderEntity order = getOrderOrThrow(request.orderId(), hotelId);
        ensureGuestCanModifyOrder(order);
        MenuItemEntity menuItem = getMenuItemOrThrow(request.menuItemId(), hotelId);
        OrderItemQuantitySupport.validate(menuItem, request.quantity(), request.weightQuantity());
        orderItemMapper.updateEntity(entity, request, order, menuItem);
        entity = orderItemRepository.save(entity);
        orderItemFolioSyncService.updateCharge(entity);
        return orderItemMapper.toResponse(entity);
    }

    @Transactional
    public OrderItemResponse patch(Long id, PatchOrderItemRequest request) {
        Long hotelId = HotelContextHolder.getHotelIdOrThrow();
        OrderItemEntity entity = getEntityOrThrow(id);
        ensureGuestCanModifyOrder(entity.getOrder());
        OrderEntity order = request.orderId() != null ? getOrderOrThrow(request.orderId(), hotelId) : null;
        if (order != null) {
            ensureGuestCanModifyOrder(order);
        }
        MenuItemEntity menuItem = request.menuItemId() != null ? getMenuItemOrThrow(request.menuItemId(), hotelId) : null;
        orderItemMapper.patchEntity(entity, request, order, menuItem);
        entity = orderItemRepository.save(entity);
        orderItemFolioSyncService.updateCharge(entity);
        return orderItemMapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public OrderItemResponse get(Long id) {
        return orderItemMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<OrderItemResponse> search(OrderItemSearchCriteria criteria, Pageable pageable) {
        Long hotelId = HotelContextHolder.getHotelIdOrThrow();
        GuestOrderAccess.Scope guestScope = GuestOrderAccess.currentGuestScope().orElse(null);
        return orderItemRepository.findAll(OrderItemSpecification.withCriteria(hotelId, criteria, guestScope), pageable)
                .map(orderItemMapper::toResponse);
    }

    @Transactional
    public void delete(Long id) {
        OrderItemEntity entity = getEntityOrThrow(id);
        ensureGuestCanModifyOrder(entity.getOrder());
        orderItemFolioSyncService.voidCharge(entity);
        orderItemRepository.delete(entity);
    }

    private OrderItemEntity getEntityOrThrow(Long id) {
        Long hotelId = HotelContextHolder.getHotelIdOrThrow();
        OrderItemEntity entity = orderItemRepository.findByIdAndHotelId(id, hotelId)
                .orElseThrow(() -> ApiExceptions.notFound(EntityKey.ORDER_ITEM));
        ensureGuestCanAccessOrder(entity.getOrder());
        return entity;
    }

    private OrderEntity getOrderOrThrow(Long orderId, Long hotelId) {
        OrderEntity order = orderRepository.findByIdAndHotelId(orderId, hotelId)
                .orElseThrow(() -> ApiExceptions.notFound(EntityKey.ORDER));
        ensureGuestCanAccessOrder(order);
        return order;
    }

    private void ensureGuestCanAccessOrder(OrderEntity order) {
        GuestOrderAccess.currentGuestScope().ifPresent(scope -> {
            if (!GuestOrderAccess.ownsOrder(order.getGuestInformation(), scope)) {
                throw ApiExceptions.notFound(EntityKey.ORDER);
            }
        });
    }

    private void ensureGuestCanModifyOrder(OrderEntity order) {
        ensureGuestCanAccessOrder(order);
        GuestOrderAccess.ensureGuestCanModify(order, utcDateTimeService.now());
    }

    private MenuItemEntity getMenuItemOrThrow(Long menuItemId, Long hotelId) {
        return menuItemRepository.findByIdAndHotelId(menuItemId, hotelId)
                .orElseThrow(() -> ApiExceptions.notFound(EntityKey.MENU_ITEM));
    }
}
