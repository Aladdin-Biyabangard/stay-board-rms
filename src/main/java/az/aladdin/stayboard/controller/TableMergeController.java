package az.aladdin.stayboard.controller;

import az.aladdin.stayboard.model.request.MergeTablesRequest;
import az.aladdin.stayboard.model.response.TableMergeGroupResponse;
import az.aladdin.stayboard.service.seating.TableMergeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/rms/tables")
public class TableMergeController {

    private final TableMergeService tableMergeService;

    @PostMapping("/merge")
    public TableMergeGroupResponse mergeTables(@Valid @RequestBody MergeTablesRequest request) {
        return tableMergeService.mergeTables(request);
    }

    @GetMapping("/{tableId}/merge-group")
    public TableMergeGroupResponse getMergeGroup(@PathVariable Long tableId) {
        return tableMergeService.getMergeGroup(tableId);
    }

    @DeleteMapping("/{tableId}/merge")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unmergeTables(@PathVariable Long tableId) {
        tableMergeService.unmergeTables(tableId);
    }
}
