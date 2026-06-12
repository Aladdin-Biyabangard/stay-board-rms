package az.aladdin.stayboard.controller;

import az.aladdin.stayboard.model.request.CreateAllergenRequest;
import az.aladdin.stayboard.model.request.PatchAllergenRequest;
import az.aladdin.stayboard.model.request.search.AllergenSearchCriteria;
import az.aladdin.stayboard.model.response.AllergenResponse;
import az.aladdin.stayboard.service.menu.AllergenService;
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
@RequestMapping("/v1/rms/allergens")
public class AllergenController {

    private final AllergenService allergenService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AllergenResponse create(@Valid @RequestBody CreateAllergenRequest request) {
        return allergenService.create(request);
    }

    @PutMapping("/{id}")
    public AllergenResponse update(@PathVariable Long id, @Valid @RequestBody CreateAllergenRequest request) {
        return allergenService.update(id, request);
    }

    @PatchMapping("/{id}")
    public AllergenResponse patch(@PathVariable Long id, @RequestBody PatchAllergenRequest request) {
        return allergenService.patch(id, request);
    }

    @GetMapping("/{id}")
    public AllergenResponse get(@PathVariable Long id) {
        return allergenService.get(id);
    }

    @GetMapping
    public ResponseEntity<List<AllergenResponse>> search(AllergenSearchCriteria criteria, Pageable pageable) {
        return PageResponseUtil.ok(allergenService.search(criteria, pageable));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        allergenService.delete(id);
    }
}
