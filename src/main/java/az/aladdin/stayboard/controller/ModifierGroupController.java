package az.aladdin.stayboard.controller;

import az.aladdin.stayboard.model.request.CreateModifierGroupRequest;
import az.aladdin.stayboard.model.request.PatchModifierGroupRequest;
import az.aladdin.stayboard.model.request.search.ModifierGroupSearchCriteria;
import az.aladdin.stayboard.model.response.ModifierGroupResponse;
import az.aladdin.stayboard.service.menu.ModifierGroupService;
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
@RequestMapping("/v1/rms/modifier-groups")
public class ModifierGroupController {

    private final ModifierGroupService modifierGroupService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ModifierGroupResponse create(@Valid @RequestBody CreateModifierGroupRequest request) {
        return modifierGroupService.create(request);
    }

    @PutMapping("/{id}")
    public ModifierGroupResponse update(@PathVariable Long id, @Valid @RequestBody CreateModifierGroupRequest request) {
        return modifierGroupService.update(id, request);
    }

    @PatchMapping("/{id}")
    public ModifierGroupResponse patch(@PathVariable Long id, @RequestBody PatchModifierGroupRequest request) {
        return modifierGroupService.patch(id, request);
    }

    @GetMapping("/{id}")
    public ModifierGroupResponse get(@PathVariable Long id) {
        return modifierGroupService.get(id);
    }

    @GetMapping
    public ResponseEntity<List<ModifierGroupResponse>> search(ModifierGroupSearchCriteria criteria, Pageable pageable) {
        return PageResponseUtil.ok(modifierGroupService.search(criteria, pageable));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        modifierGroupService.delete(id);
    }
}
