package az.aladdin.stayboard.controller;

import az.aladdin.stayboard.model.request.CreateTableRequest;
import az.aladdin.stayboard.model.request.PatchTableRequest;
import az.aladdin.stayboard.model.request.UpdateTableRequest;
import az.aladdin.stayboard.model.request.search.TableSearchCriteria;
import az.aladdin.stayboard.model.response.TableAvailabilityResponse;
import az.aladdin.stayboard.model.response.TableResponse;
import az.aladdin.stayboard.service.TableAvailabilityService;
import az.aladdin.stayboard.service.TableService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/rms/tables")
public class TableController {

    private final TableService tableService;
    private final TableAvailabilityService tableAvailabilityService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TableResponse create(@Valid @RequestBody CreateTableRequest request) {
        return tableService.create(request);
    }

    @PutMapping("/{id}")
    public TableResponse update(@PathVariable Long id, @Valid @RequestBody UpdateTableRequest request) {
        return tableService.update(id, request);
    }

    @PatchMapping("/{id}")
    public TableResponse patch(@PathVariable Long id, @RequestBody PatchTableRequest request) {
        return tableService.patch(id, request);
    }

    @GetMapping("/availability")
    public List<TableAvailabilityResponse> availability(
            @RequestParam(required = false) LocalDateTime at,
            @RequestParam(required = false) Integer durationMinutes,
            @RequestParam(required = false) Integer partySize,
            @RequestParam(required = false) Long reservationId
    ) {
        return tableAvailabilityService.getAvailability(at, durationMinutes, partySize, reservationId);
    }

    @GetMapping("/{id}")
    public TableResponse get(@PathVariable Long id) {
        return tableService.get(id);
    }

    @GetMapping
    public ResponseEntity<List<TableResponse>> search(TableSearchCriteria criteria, Pageable pageable) {
        return PageResponseUtil.ok(tableService.search(criteria, pageable));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        tableService.delete(id);
    }
}
