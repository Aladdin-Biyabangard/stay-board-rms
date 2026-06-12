package az.aladdin.stayboard.controller;

import az.aladdin.stayboard.model.request.CreateDietaryTagRequest;
import az.aladdin.stayboard.model.request.PatchDietaryTagRequest;
import az.aladdin.stayboard.model.request.search.DietaryTagSearchCriteria;
import az.aladdin.stayboard.model.response.DietaryTagResponse;
import az.aladdin.stayboard.service.menu.DietaryTagService;
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
@RequestMapping("/v1/rms/dietary-tags")
public class DietaryTagController {

    private final DietaryTagService dietaryTagService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DietaryTagResponse create(@Valid @RequestBody CreateDietaryTagRequest request) {
        return dietaryTagService.create(request);
    }

    @PutMapping("/{id}")
    public DietaryTagResponse update(@PathVariable Long id, @Valid @RequestBody CreateDietaryTagRequest request) {
        return dietaryTagService.update(id, request);
    }

    @PatchMapping("/{id}")
    public DietaryTagResponse patch(@PathVariable Long id, @RequestBody PatchDietaryTagRequest request) {
        return dietaryTagService.patch(id, request);
    }

    @GetMapping("/{id}")
    public DietaryTagResponse get(@PathVariable Long id) {
        return dietaryTagService.get(id);
    }

    @GetMapping
    public ResponseEntity<List<DietaryTagResponse>> search(DietaryTagSearchCriteria criteria, Pageable pageable) {
        return PageResponseUtil.ok(dietaryTagService.search(criteria, pageable));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        dietaryTagService.delete(id);
    }
}
