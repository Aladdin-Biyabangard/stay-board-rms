package az.aladdin.stayboard.controller;

import az.aladdin.stayboard.model.request.AdjustInventoryStockRequest;
import az.aladdin.stayboard.model.request.CreateInventoryItemRequest;
import az.aladdin.stayboard.model.request.PatchInventoryItemRequest;
import az.aladdin.stayboard.model.request.UpdateInventoryItemRequest;
import az.aladdin.stayboard.model.request.search.InventoryItemSearchCriteria;
import az.aladdin.stayboard.model.response.InventoryItemResponse;
import az.aladdin.stayboard.service.inventory.InventoryItemService;
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
@RequestMapping("/v1/rms/inventory-items")
public class InventoryItemController {

    private final InventoryItemService inventoryItemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryItemResponse create(@Valid @RequestBody CreateInventoryItemRequest request) {
        return inventoryItemService.create(request);
    }

    @PutMapping("/{id}")
    public InventoryItemResponse update(@PathVariable Long id, @Valid @RequestBody UpdateInventoryItemRequest request) {
        return inventoryItemService.update(id, request);
    }

    @PatchMapping("/{id}")
    public InventoryItemResponse patch(@PathVariable Long id, @RequestBody PatchInventoryItemRequest request) {
        return inventoryItemService.patch(id, request);
    }

    @PostMapping("/{id}/stock")
    public InventoryItemResponse adjustStock(@PathVariable Long id, @Valid @RequestBody AdjustInventoryStockRequest request) {
        return inventoryItemService.adjustStock(id, request);
    }

    @GetMapping("/{id}")
    public InventoryItemResponse get(@PathVariable Long id) {
        return inventoryItemService.get(id);
    }

    @GetMapping
    public ResponseEntity<List<InventoryItemResponse>> search(InventoryItemSearchCriteria criteria, Pageable pageable) {
        return PageResponseUtil.ok(inventoryItemService.search(criteria, pageable));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        inventoryItemService.delete(id);
    }
}
