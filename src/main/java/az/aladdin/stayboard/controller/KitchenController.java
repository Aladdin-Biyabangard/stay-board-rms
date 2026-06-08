package az.aladdin.stayboard.controller;

import az.aladdin.stayboard.model.request.UpdateKitchenTicketStatusRequest;
import az.aladdin.stayboard.model.request.search.KitchenTicketSearchCriteria;
import az.aladdin.stayboard.model.response.KitchenTicketResponse;
import az.aladdin.stayboard.service.KitchenService;
import az.aladdin.stayboard.util.PageResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/rms/kitchen/tickets")
public class KitchenController {

    private final KitchenService kitchenService;

    @GetMapping
    public ResponseEntity<List<KitchenTicketResponse>> searchTickets(
            KitchenTicketSearchCriteria criteria,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return PageResponseUtil.ok(kitchenService.searchTickets(criteria, pageable));
    }

    @PatchMapping("/{orderItemId}/status")
    public KitchenTicketResponse updateStatus(
            @PathVariable Long orderItemId,
            @Valid @RequestBody UpdateKitchenTicketStatusRequest request
    ) {
        return kitchenService.updateStatus(orderItemId, request);
    }
}
