package az.aladdin.stayboard.service.order;

import az.aladdin.stayboard.entity.OrderEntity;
import az.aladdin.stayboard.entity.TableEntity;
import az.aladdin.stayboard.exception.ApiExceptions;
import az.aladdin.stayboard.exception.EntityKey;
import az.aladdin.stayboard.exception.MessageKey;
import az.aladdin.stayboard.mapper.OrderMapper;
import az.aladdin.stayboard.model.enums.OrderStatus;
import az.aladdin.stayboard.model.request.CreateOrderRequest;
import az.aladdin.stayboard.model.request.PatchOrderRequest;
import az.aladdin.stayboard.model.request.UpdateOrderRequest;
import az.aladdin.stayboard.model.request.search.OrderSearchCriteria;
import az.aladdin.stayboard.model.response.OrderResponse;
import az.aladdin.stayboard.repository.OrderItemRepository;
import az.aladdin.stayboard.repository.OrderRepository;
import az.aladdin.stayboard.repository.TableRepository;
import az.aladdin.stayboard.security.AuthenticatedUserSupport;
import az.aladdin.stayboard.security.GuestOrderAccess;
import az.aladdin.stayboard.service.common.UtcDateTimeService;
import az.aladdin.stayboard.service.seating.TableAvailabilityService;
import az.aladdin.stayboard.service.hotel.HotelAwareService;
import az.aladdin.stayboard.service.hotel.HotelTimeService;
import az.aladdin.stayboard.specification.OrderSpecification;
import az.aladdin.stayboard.util.OrderNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.BiFunction;

@Service
@RequiredArgsConstructor
public class OrderService extends HotelAwareService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final TableRepository tableRepository;
    private final OrderMapper orderMapper;
    private final UtcDateTimeService utcDateTimeService;
    private final HotelTimeService hotelTimeService;
    private final OrderItemFolioSyncService orderItemFolioSyncService;
    private final TableAvailabilityService tableAvailabilityService;
    private final OrderItemService orderItemService;
    private final OrderNumberGenerator orderNumberGenerator;

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        Long hotelId = getCurrentHotelId();
        TableEntity tableEntity = resolveTable(request.tableId(), hotelId);
        ensureTableAvailableForOrder(hotelId, tableEntity);
        String orderNumber = orderNumberGenerator.generate(hotelId);
        OrderEntity entity = orderMapper.toEntity(request, hotelId, orderNumber, tableEntity);
        if (AuthenticatedUserSupport.isGuest()) {
            entity.setGuestInformation(GuestOrderAccess.attachGuestUserId(
                    entity.getGuestInformation(),
                    AuthenticatedUserSupport.requirePrincipal().getUserId()
            ));
        }
        entity = orderRepository.save(entity);
        if (request.items() != null && !request.items().isEmpty()) {
            orderItemService.createLinesForOrder(entity, request.items());
            entity = fetchOrderEntity(entity.getId());
        }
        return orderMapper.toResponse(entity);
    }

    @Transactional
    public OrderResponse update(Long id, UpdateOrderRequest request) {
        return saveModifiedOrder(id, request.orderStatus(), (entity, hotelId) -> {
            TableEntity tableEntity = AuthenticatedUserSupport.isGuest()
                    ? entity.getTableEntity()
                    : resolveTable(request.tableId(), hotelId);
            orderMapper.updateEntity(entity, request, tableEntity);
            return tableEntity;
        });
    }

    @Transactional
    public OrderResponse patch(Long id, PatchOrderRequest request) {
        return saveModifiedOrder(id, request.orderStatus(), (entity, hotelId) -> {
            TableEntity tableEntity = AuthenticatedUserSupport.isGuest() || request.tableId() == null
                    ? entity.getTableEntity()
                    : resolveTable(request.tableId(), hotelId);
            orderMapper.patchEntity(entity, request, tableEntity);
            return tableEntity;
        });
    }

    @Transactional(readOnly = true)
    public OrderResponse get(Long id) {
        return orderMapper.toResponse(fetchOrderEntity(id));
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> search(OrderSearchCriteria criteria, Pageable pageable) {
        Long hotelId = getCurrentHotelId();
        Pageable sortedPageable = pageable.getSort().isSorted()
                ? pageable
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        GuestOrderAccess.Scope guestScope = GuestOrderAccess.currentGuestScope().orElse(null);
        OrderSearchCriteria normalizedCriteria = normalizeSearchCriteria(criteria, hotelId);
        return orderRepository.findAll(OrderSpecification.withCriteria(hotelId, normalizedCriteria, guestScope), sortedPageable)
                .map(orderMapper::toResponse);
    }

    @Transactional
    public void delete(Long id) {
        OrderEntity entity = fetchOrderEntity(id);
        if (orderItemRepository.existsByOrder_IdAndHotelId(entity.getId(), entity.getHotelId())) {
            throw ApiExceptions.conflict(MessageKey.CONFLICT_ORDER_HAS_ITEMS);
        }
        orderRepository.delete(entity);
    }

    private OrderResponse saveModifiedOrder(
            Long id,
            OrderStatus requestedStatus,
            BiFunction<OrderEntity, Long, TableEntity> mutator
    ) {
        OrderEntity entity = fetchOrderEntity(id);
        GuestOrderAccess.ensureGuestCanModify(entity, utcDateTimeService.now());
        GuestOrderAccess.ensureGuestAllowedOrderStatusChange(requestedStatus, entity.getOrderStatus());
        OrderStatus previousStatus = entity.getOrderStatus();
        Long preservedGuestUserId = preservedGuestUserId(entity);
        mutator.apply(entity, getCurrentHotelId());
        restoreGuestOwnership(entity, preservedGuestUserId);
        entity = orderRepository.save(entity);
        voidFolioChargesIfCancelled(entity, previousStatus);
        return orderMapper.toResponse(entity);
    }

    private OrderEntity fetchOrderEntity(Long id) {
        OrderEntity entity = orderRepository.findByIdAndHotelId(id, getCurrentHotelId())
                .orElseThrow(() -> ApiExceptions.notFound(EntityKey.ORDER));
        GuestOrderAccess.ensureGuestCanAccessOrder(entity);
        return entity;
    }

    private TableEntity resolveTable(Long tableId, Long hotelId) {
        if (tableId == null) {
            return null;
        }
        return tableRepository.findByIdAndHotelId(tableId, hotelId)
                .orElseThrow(() -> ApiExceptions.notFound(EntityKey.TABLE));
    }

    private void ensureTableAvailableForOrder(Long hotelId, TableEntity tableEntity) {
        if (tableEntity == null) {
            return;
        }
        java.time.LocalDateTime nowUtc = utcDateTimeService.now();
        java.time.LocalDateTime endUtc = nowUtc.plusMinutes(TableAvailabilityService.DEFAULT_DINING_DURATION_MINUTES);
        if (!tableAvailabilityService.isTableReservable(hotelId, tableEntity, nowUtc, endUtc, null)) {
            throw ApiExceptions.conflict(MessageKey.CONFLICT_TABLE_NOT_AVAILABLE);
        }
    }

    private Long preservedGuestUserId(OrderEntity entity) {
        return entity.getGuestInformation() != null ? entity.getGuestInformation().guestUserId() : null;
    }

    private void restoreGuestOwnership(OrderEntity entity, Long guestUserId) {
        if (!AuthenticatedUserSupport.isGuest()) {
            return;
        }
        entity.setGuestInformation(GuestOrderAccess.attachGuestUserId(entity.getGuestInformation(), guestUserId));
    }

    private void voidFolioChargesIfCancelled(OrderEntity entity, OrderStatus previousStatus) {
        if (previousStatus == OrderStatus.CANCELLED || entity.getOrderStatus() != OrderStatus.CANCELLED) {
            return;
        }
        orderItemFolioSyncService.voidChargesForOrder(entity);
    }

    private OrderSearchCriteria normalizeSearchCriteria(OrderSearchCriteria criteria, Long hotelId) {
        if (criteria == null) {
            return null;
        }
        if (criteria.createdFrom() == null && criteria.createdTo() == null) {
            return criteria;
        }
        return new OrderSearchCriteria(
                criteria.orderNumber(),
                criteria.tableId(),
                criteria.roomNumber(),
                criteria.orderStatus(),
                criteria.guestFirstName(),
                criteria.guestLastName(),
                hotelTimeService.hotelLocalDateTimeToUtc(criteria.createdFrom(), hotelId),
                hotelTimeService.hotelLocalDateTimeToUtc(criteria.createdTo(), hotelId)
        );
    }
}
