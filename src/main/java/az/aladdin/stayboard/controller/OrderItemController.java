package az.aladdin.stayboard.controller;

import az.aladdin.stayboard.model.request.CreateOrderItemRequest;
import az.aladdin.stayboard.model.request.PatchOrderItemRequest;
import az.aladdin.stayboard.model.request.UpdateOrderItemRequest;
import az.aladdin.stayboard.model.request.search.OrderItemSearchCriteria;
import az.aladdin.stayboard.model.response.OrderItemResponse;
import az.aladdin.stayboard.service.order.OrderItemService;
import az.aladdin.stayboard.util.PageResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/rms/order-items")
public class OrderItemController {

    private final OrderItemService orderItemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderItemResponse create(@Valid @RequestBody CreateOrderItemRequest request) {
        return orderItemService.create(request);
    }

    @PutMapping("/{id}")
    public OrderItemResponse update(@PathVariable Long id, @Valid @RequestBody UpdateOrderItemRequest request) {
        return orderItemService.update(id, request);
    }

    @PatchMapping("/{id}")
    public OrderItemResponse patch(@PathVariable Long id, @RequestBody PatchOrderItemRequest request) {
        return orderItemService.patch(id, request);
    }

    @GetMapping("/{id}")
    public OrderItemResponse get(@PathVariable Long id) {
        return orderItemService.get(id);
    }

    @GetMapping
    public ResponseEntity<List<OrderItemResponse>> search(OrderItemSearchCriteria criteria, Pageable pageable) {
        return PageResponseUtil.ok(orderItemService.search(criteria, pageable));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        orderItemService.delete(id);
    }
}
