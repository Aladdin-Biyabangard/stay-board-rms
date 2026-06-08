package az.aladdin.stayboard.controller;

import az.aladdin.stayboard.model.request.CreateMenuItemRequest;
import az.aladdin.stayboard.model.request.PatchMenuItemRequest;
import az.aladdin.stayboard.model.request.UpdateMenuItemRequest;
import az.aladdin.stayboard.model.request.search.MenuItemSearchCriteria;
import az.aladdin.stayboard.model.response.MenuItemResponse;
import az.aladdin.stayboard.service.MenuItemService;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/rms/menu-items")
public class MenuItemController {

    private final MenuItemService menuItemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MenuItemResponse create(@Valid @RequestBody CreateMenuItemRequest request) {
        return menuItemService.create(request);
    }

    @PutMapping("/{id}")
    public MenuItemResponse update(@PathVariable Long id, @Valid @RequestBody UpdateMenuItemRequest request) {
        return menuItemService.update(id, request);
    }

    @PatchMapping("/{id}")
    public MenuItemResponse patch(@PathVariable Long id, @RequestBody PatchMenuItemRequest request) {
        return menuItemService.patch(id, request);
    }

    @GetMapping("/{id}")
    public MenuItemResponse get(@PathVariable Long id) {
        return menuItemService.get(id);
    }

    @GetMapping
    public ResponseEntity<List<MenuItemResponse>> search(MenuItemSearchCriteria criteria, Pageable pageable) {
        return PageResponseUtil.ok(menuItemService.search(criteria, pageable));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        menuItemService.delete(id);
    }

    @PostMapping(value = "/{id}/images", consumes = "multipart/form-data")
    public MenuItemResponse uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return menuItemService.uploadImage(id, file);
    }

    @DeleteMapping("/{id}/images")
    public MenuItemResponse deleteImage(@PathVariable Long id, @RequestParam("imageUrl") String imageUrl) {
        return menuItemService.deleteImage(id, imageUrl);
    }

    @PutMapping("/{id}/images/main")
    public MenuItemResponse setMainImage(@PathVariable Long id, @RequestParam("imageUrl") String imageUrl) {
        return menuItemService.setMainImage(id, imageUrl);
    }
}
