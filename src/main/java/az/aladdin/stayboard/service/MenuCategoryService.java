package az.aladdin.stayboard.service;

import az.aladdin.stayboard.annotation.NoLogging;
import az.aladdin.stayboard.context.HotelContextHolder;
import az.aladdin.stayboard.entity.MenuCategoryEntity;
import az.aladdin.stayboard.exception.ApiExceptions;
import az.aladdin.stayboard.exception.EntityKey;
import az.aladdin.stayboard.exception.MessageKey;
import az.aladdin.stayboard.mapper.MenuCategoryMapper;
import az.aladdin.stayboard.model.request.CreateMenuCategoryRequest;
import az.aladdin.stayboard.model.request.PatchMenuCategoryRequest;
import az.aladdin.stayboard.model.request.UpdateMenuCategoryRequest;
import az.aladdin.stayboard.model.request.search.MenuCategorySearchCriteria;
import az.aladdin.stayboard.model.response.FileUploadResponse;
import az.aladdin.stayboard.model.response.MenuCategoryResponse;
import az.aladdin.stayboard.repository.MenuCategoryRepository;
import az.aladdin.stayboard.repository.MenuItemRepository;
import az.aladdin.stayboard.service.common.FileLoadService;
import az.aladdin.stayboard.specification.MenuCategorySpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuCategoryService {

    private static final String MENU_CATEGORY_PHOTO_KEY = "menuCategoryPhoto";

    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final MenuCategoryMapper menuCategoryMapper;
    private final FileLoadService fileLoadService;

    @Transactional
    public MenuCategoryResponse create(CreateMenuCategoryRequest request) {
        Long hotelId = HotelContextHolder.getHotelIdOrThrow();
        MenuCategoryEntity entity = menuCategoryMapper.toEntity(request, hotelId);
        return menuCategoryMapper.toResponse(menuCategoryRepository.save(entity));
    }

    @Transactional
    public MenuCategoryResponse update(Long id, CreateMenuCategoryRequest request) {
        MenuCategoryEntity entity = getEntityOrThrow(id);
        menuCategoryMapper.updateEntity(entity, request);
        return menuCategoryMapper.toResponse(menuCategoryRepository.save(entity));
    }

    @Transactional
    public MenuCategoryResponse patch(Long id, PatchMenuCategoryRequest request) {
        MenuCategoryEntity entity = getEntityOrThrow(id);
        menuCategoryMapper.patchEntity(entity, request);
        return menuCategoryMapper.toResponse(menuCategoryRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public MenuCategoryResponse get(Long id) {
        return menuCategoryMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<MenuCategoryResponse> search(MenuCategorySearchCriteria criteria, Pageable pageable) {
        Long hotelId = HotelContextHolder.getHotelIdOrThrow();
        return menuCategoryRepository.findAll(MenuCategorySpecification.withCriteria(hotelId, criteria), pageable)
                .map(menuCategoryMapper::toResponse);
    }

    @Transactional
    public void delete(Long id) {
        MenuCategoryEntity entity = getEntityOrThrow(id);
        if (menuItemRepository.existsByMenuCategory_IdAndHotelId(entity.getId(), entity.getHotelId())) {
            throw ApiExceptions.conflict(MessageKey.CONFLICT_MENU_CATEGORY_HAS_ITEMS);
        }
        deleteAllImages(entity);
        menuCategoryRepository.delete(entity);
    }

    @NoLogging
    @Transactional
    public MenuCategoryResponse uploadImage(Long id, MultipartFile file) {
        MenuCategoryEntity entity = getEntityOrThrow(id);
        FileUploadResponse upload;
        try {
            upload = fileLoadService.uploadFile(file, String.valueOf(id), MENU_CATEGORY_PHOTO_KEY);
        } catch (IOException e) {
            log.error("Menu category image upload failed for menuCategoryId={}", id, e);
            throw ApiExceptions.badRequest(MessageKey.BAD_REQUEST_FILE_UPLOAD_FAILED);
        }

        String imageUrl = upload.getUrl();
        entity.getPhotoUrls().add(imageUrl);
        if (entity.getMainImageUrl() == null || entity.getMainImageUrl().isEmpty()) {
            entity.setMainImageUrl(imageUrl);
        }
        return menuCategoryMapper.toResponse(menuCategoryRepository.save(entity));
    }

    @Transactional
    public MenuCategoryResponse deleteImage(Long id, String imageUrl) {
        MenuCategoryEntity entity = getEntityOrThrow(id);
        String targetImageUrl = findGalleryImage(entity, imageUrl);
        if (entity.getPhotoUrls().remove(targetImageUrl)) {
            if (targetImageUrl.equals(entity.getMainImageUrl())) {
                entity.setMainImageUrl(
                        entity.getPhotoUrls().isEmpty() ? null : entity.getPhotoUrls().iterator().next()
                );
            }
            menuCategoryRepository.save(entity);
            fileLoadService.deleteByPublicUrl(targetImageUrl);
        }
        return menuCategoryMapper.toResponse(entity);
    }

    @Transactional
    public MenuCategoryResponse setMainImage(Long id, String imageUrl) {
        MenuCategoryEntity entity = getEntityOrThrow(id);
        if (!entity.getPhotoUrls().contains(imageUrl)) {
            throw ApiExceptions.imageNotInGallery(EntityKey.MENU_CATEGORY, imageUrl);
        }
        entity.setMainImageUrl(imageUrl);
        return menuCategoryMapper.toResponse(menuCategoryRepository.save(entity));
    }

    private String findGalleryImage(MenuCategoryEntity entity, String imageUrl) {
        String targetKey = fileLoadService.resolveKey(imageUrl);
        return entity.getPhotoUrls().stream()
                .filter(photoUrl -> {
                    String photoKey = fileLoadService.resolveKey(photoUrl);
                    return photoUrl.equals(imageUrl) || (photoKey != null && photoKey.equals(targetKey));
                })
                .findFirst()
                .orElseThrow(() -> ApiExceptions.imageNotInGallery(EntityKey.MENU_CATEGORY, imageUrl));
    }

    private void deleteAllImages(MenuCategoryEntity entity) {
        new ArrayList<>(entity.getPhotoUrls()).forEach(fileLoadService::deleteByPublicUrl);
        entity.getPhotoUrls().clear();
        entity.setMainImageUrl(null);
    }

    private MenuCategoryEntity getEntityOrThrow(Long id) {
        Long hotelId = HotelContextHolder.getHotelIdOrThrow();
        return menuCategoryRepository.findByIdAndHotelId(id, hotelId)
                .orElseThrow(() -> ApiExceptions.notFound(EntityKey.MENU_CATEGORY));
    }
}
