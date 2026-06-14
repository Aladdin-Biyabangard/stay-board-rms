package az.aladdin.stayboard.controller;

import az.aladdin.stayboard.model.request.CreateModifierOptionRequest;
import az.aladdin.stayboard.model.request.PatchModifierOptionRequest;
import az.aladdin.stayboard.model.request.search.ModifierOptionSearchCriteria;
import az.aladdin.stayboard.model.response.ModifierOptionResponse;
import az.aladdin.stayboard.service.menu.ModifierOptionService;
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
@RequestMapping("/v1/rms/modifier-options")
public class ModifierOptionController {

    private final ModifierOptionService modifierOptionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ModifierOptionResponse create(@Valid @RequestBody CreateModifierOptionRequest request) {
        return modifierOptionService.create(request);
    }

    @PutMapping("/{id}")
    public ModifierOptionResponse update(@PathVariable Long id, @Valid @RequestBody CreateModifierOptionRequest request) {
        return modifierOptionService.update(id, request);
    }

    @PatchMapping("/{id}")
    public ModifierOptionResponse patch(@PathVariable Long id, @RequestBody PatchModifierOptionRequest request) {
        return modifierOptionService.patch(id, request);
    }

    @GetMapping("/{id}")
    public ModifierOptionResponse get(@PathVariable Long id) {
        return modifierOptionService.get(id);
    }

    @GetMapping
    public ResponseEntity<List<ModifierOptionResponse>> search(ModifierOptionSearchCriteria criteria, Pageable pageable) {
        return PageResponseUtil.ok(modifierOptionService.search(criteria, pageable));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        modifierOptionService.delete(id);
    }
}
