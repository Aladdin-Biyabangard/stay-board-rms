package az.aladdin.stayboard.controller;

import az.aladdin.stayboard.model.request.CreateMenuCategoryRequest;
import az.aladdin.stayboard.model.request.PatchMenuCategoryRequest;
import az.aladdin.stayboard.model.request.UpdateMenuCategoryRequest;
import az.aladdin.stayboard.model.request.search.MenuCategorySearchCriteria;
import az.aladdin.stayboard.model.response.MenuCategoryResponse;
import az.aladdin.stayboard.service.MenuCategoryService;
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
@RequestMapping("/v1/rms/menu-categories")
public class MenuCategoryController {

    private final MenuCategoryService menuCategoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MenuCategoryResponse create(@Valid @RequestBody CreateMenuCategoryRequest request) {
        return menuCategoryService.create(request);
    }

    @PutMapping("/{id}")
    public MenuCategoryResponse update(@PathVariable Long id, @Valid @RequestBody CreateMenuCategoryRequest request) {
        return menuCategoryService.update(id, request);
    }

    @PatchMapping("/{id}")
    public MenuCategoryResponse patch(@PathVariable Long id, @RequestBody PatchMenuCategoryRequest request) {
        return menuCategoryService.patch(id, request);
    }

    @GetMapping("/{id}")
    public MenuCategoryResponse get(@PathVariable Long id) {
        return menuCategoryService.get(id);
    }

    @GetMapping
    public ResponseEntity<List<MenuCategoryResponse>> search(MenuCategorySearchCriteria criteria, Pageable pageable) {
        return PageResponseUtil.ok(menuCategoryService.search(criteria, pageable));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        menuCategoryService.delete(id);
    }

    @PostMapping(value = "/{id}/images", consumes = "multipart/form-data")
    public MenuCategoryResponse uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return menuCategoryService.uploadImage(id, file);
    }

    @DeleteMapping("/{id}/images")
    public MenuCategoryResponse deleteImage(@PathVariable Long id, @RequestParam("imageUrl") String imageUrl) {
        return menuCategoryService.deleteImage(id, imageUrl);
    }

    @PutMapping("/{id}/images/main")
    public MenuCategoryResponse setMainImage(@PathVariable Long id, @RequestParam("imageUrl") String imageUrl) {
        return menuCategoryService.setMainImage(id, imageUrl);
    }
}
