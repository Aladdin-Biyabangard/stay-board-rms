package az.aladdin.stayboard.service.order;

import az.aladdin.stayboard.port.FolioPort;
import az.aladdin.stayboard.entity.OrderEntity;
import az.aladdin.stayboard.entity.OrderItemEntity;
import az.aladdin.stayboard.exception.ApiExceptions;
import az.aladdin.stayboard.exception.MessageKey;
import az.aladdin.stayboard.model.enums.FolioChargeType;
import az.aladdin.stayboard.model.request.folio.AddFolioChargeRequest;
import az.aladdin.stayboard.model.request.folio.VoidFolioChargeRequest;
import az.aladdin.stayboard.model.response.folio.FolioChargeResponse;
import az.aladdin.stayboard.repository.OrderItemRepository;
import az.aladdin.stayboard.util.OrderItemPricingSupport;
import az.aladdin.stayboard.util.OrderItemPricingSupport.FolioChargePosting;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderItemFolioSyncService {

    private static final String VOID_REASON_REMOVED = "RMS order item removed";
    private static final String VOID_REASON_UPDATED = "RMS order item updated";

    private final FolioPort folioPort;
    private final OrderItemRepository orderItemRepository;

    public void postCharge(OrderItemEntity orderItem) {
        OrderEntity order = orderItem.getOrder();
        if (!shouldSyncToFolio(order)) {
            return;
        }

        AddFolioChargeRequest request = buildChargeRequest(orderItem);
        FolioChargeResponse response = folioPort.addChargeByRoom(order.getRoomNumber().trim(), request);
        if (response == null || response.getId() == null) {
            throw ApiExceptions.badRequest(MessageKey.BAD_REQUEST_FOLIO_CHARGE_SYNC_FAILED);
        }
        orderItem.setFolioChargeId(response.getId());
        orderItemRepository.save(orderItem);
    }

    public void voidCharge(OrderItemEntity orderItem) {
        if (orderItem.getFolioChargeId() == null) {
            return;
        }

        folioPort.voidCharge(
                orderItem.getFolioChargeId(),
                VoidFolioChargeRequest.builder().reason(VOID_REASON_REMOVED).build()
        );
        orderItem.setFolioChargeId(null);
        orderItemRepository.save(orderItem);
    }

    public void updateCharge(OrderItemEntity orderItem) {
        if (orderItem.getFolioChargeId() != null) {
            folioPort.voidCharge(
                    orderItem.getFolioChargeId(),
                    VoidFolioChargeRequest.builder().reason(VOID_REASON_UPDATED).build()
            );
            orderItem.setFolioChargeId(null);
        }
        postCharge(orderItem);
    }

    public void voidChargesForOrder(OrderEntity order) {
        if (!shouldSyncToFolio(order)) {
            return;
        }
        List<OrderItemEntity> items = orderItemRepository.findAllByOrder_IdAndHotelId(order.getId(), order.getHotelId());
        for (OrderItemEntity item : items) {
            voidCharge(item);
        }
    }

    private boolean shouldSyncToFolio(OrderEntity order) {
        return order != null && order.getRoomNumber() != null && !order.getRoomNumber().isBlank();
    }

    private AddFolioChargeRequest buildChargeRequest(OrderItemEntity orderItem) {
        OrderEntity order = orderItem.getOrder();
        FolioChargePosting posting = OrderItemPricingSupport.buildFolioPosting(orderItem);
        String chargeName = buildChargeName(orderItem);
        String description = buildChargeDescription(order, orderItem);

        return AddFolioChargeRequest.builder()
                .chargeName(chargeName)
                .description(description)
                .unitPrice(posting.unitPrice())
                .quantity(posting.quantity())
                .taxRate(posting.taxRate())
                .taxType(posting.taxType())
                .chargeType(FolioChargeType.RESTAURANT)
                .build();
    }

    private String buildChargeName(OrderItemEntity orderItem) {
        String itemName = orderItem.getMenuItem() != null ? orderItem.getMenuItem().getItemName() : "Restaurant order";
        if (orderItem.getModifiers() == null || orderItem.getModifiers().isEmpty()) {
            return itemName;
        }
        String modifiers = orderItem.getModifiers().stream()
                .map(modifier -> modifier.getModifierName() != null ? modifier.getModifierName() : "")
                .filter(name -> !name.isBlank())
                .collect(Collectors.joining(", "));
        if (modifiers.isBlank()) {
            return itemName;
        }
        return itemName + " (" + modifiers + ")";
    }

    private String buildChargeDescription(OrderEntity order, OrderItemEntity orderItem) {
        return "Order #" + order.getOrderNumber() + " [RMS-OI:" + orderItem.getId() + "]";
    }
}
