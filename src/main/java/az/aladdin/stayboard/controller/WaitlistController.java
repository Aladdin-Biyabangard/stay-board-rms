package az.aladdin.stayboard.controller;

import az.aladdin.stayboard.model.request.CreateWaitlistEntryRequest;
import az.aladdin.stayboard.model.request.SeatWaitlistEntryRequest;
import az.aladdin.stayboard.model.request.UpdateWaitlistStatusRequest;
import az.aladdin.stayboard.model.request.search.WaitlistSearchCriteria;
import az.aladdin.stayboard.model.response.WaitlistEntryResponse;
import az.aladdin.stayboard.service.seating.WaitlistService;
import az.aladdin.stayboard.util.PageResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/rms/waitlist-entries")
public class WaitlistController {

    private final WaitlistService waitlistService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WaitlistEntryResponse create(@Valid @RequestBody CreateWaitlistEntryRequest request) {
        return waitlistService.create(request);
    }

    @GetMapping("/{id}")
    public WaitlistEntryResponse get(@PathVariable Long id) {
        return waitlistService.get(id);
    }

    @GetMapping
    public ResponseEntity<List<WaitlistEntryResponse>> search(
            WaitlistSearchCriteria criteria,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return PageResponseUtil.ok(waitlistService.search(criteria, pageable));
    }

    @PatchMapping("/{id}/status")
    public WaitlistEntryResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWaitlistStatusRequest request
    ) {
        return waitlistService.updateStatus(id, request);
    }

    @PostMapping("/{id}/seat")
    public WaitlistEntryResponse seat(
            @PathVariable Long id,
            @Valid @RequestBody SeatWaitlistEntryRequest request
    ) {
        return waitlistService.seat(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long id) {
        waitlistService.cancel(id);
    }
}
