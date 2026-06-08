package az.aladdin.stayboard.controller;

import az.aladdin.stayboard.model.request.CreateTableOccupancyRequest;
import az.aladdin.stayboard.model.request.search.TableOccupancySearchCriteria;
import az.aladdin.stayboard.model.response.TableOccupancyResponse;
import az.aladdin.stayboard.service.TableOccupancyService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/rms/table-occupancies")
public class TableOccupancyController {

    private final TableOccupancyService tableOccupancyService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TableOccupancyResponse create(@Valid @RequestBody CreateTableOccupancyRequest request) {
        return tableOccupancyService.create(request);
    }

    @GetMapping("/{id}")
    public TableOccupancyResponse get(@PathVariable Long id) {
        return tableOccupancyService.get(id);
    }

    @GetMapping
    public ResponseEntity<List<TableOccupancyResponse>> search(
            TableOccupancySearchCriteria criteria,
            @PageableDefault(sort = "startDateTime", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return PageResponseUtil.ok(tableOccupancyService.search(criteria, pageable));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        tableOccupancyService.delete(id);
    }
}
