package az.aladdin.stayboard.service;

import az.aladdin.stayboard.annotation.NoLogging;
import az.aladdin.stayboard.service.hotel.HotelAwareService;
import az.aladdin.stayboard.entity.MenuCategoryEntity;
import az.aladdin.stayboard.entity.MenuItemEntity;
import az.aladdin.stayboard.exception.ApiExceptions;
import az.aladdin.stayboard.exception.EntityKey;
import az.aladdin.stayboard.exception.MessageKey;
import az.aladdin.stayboard.mapper.MenuItemMapper;
import az.aladdin.stayboard.model.request.CreateMenuItemRequest;
import az.aladdin.stayboard.model.request.PatchMenuItemRequest;
import az.aladdin.stayboard.model.request.UpdateMenuItemRequest;
import az.aladdin.stayboard.model.request.search.MenuItemSearchCriteria;
import az.aladdin.stayboard.model.response.FileUploadResponse;
import az.aladdin.stayboard.model.response.MenuItemResponse;
import az.aladdin.stayboard.repository.MenuCategoryRepository;
import az.aladdin.stayboard.repository.MenuItemRepository;
import az.aladdin.stayboard.service.common.FileLoadService;
import az.aladdin.stayboard.specification.MenuItemSpecification;
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
public class MenuItemService extends HotelAwareService {

    private static final String MENU_ITEM_PHOTO_KEY = "menuItemPhoto";

    private final MenuItemRepository menuItemRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuItemMapper menuItemMapper;
    private final FileLoadService fileLoadService;

    @Transactional
    public MenuItemResponse create(CreateMenuItemRequest request) {
        Long hotelId = getCurrentHotelId();
        MenuCategoryEntity menuCategory = getMenuCategoryOrThrow(request.menuCategoryId(), hotelId);
        MenuItemEntity entity = menuItemMapper.toEntity(request, hotelId, menuCategory);
        return menuItemMapper.toResponse(menuItemRepository.save(entity));
    }

    @Transactional
    public MenuItemResponse update(Long id, UpdateMenuItemRequest request) {
        Long hotelId = getCurrentHotelId();
        MenuItemEntity entity = getEntityOrThrow(id);
        MenuCategoryEntity menuCategory = getMenuCategoryOrThrow(request.menuCategoryId(), hotelId);
        menuItemMapper.updateEntity(entity, request, menuCategory);
        return menuItemMapper.toResponse(menuItemRepository.save(entity));
    }

    @Transactional
    public MenuItemResponse patch(Long id, PatchMenuItemRequest request) {
        Long hotelId = getCurrentHotelId();
        MenuItemEntity entity = getEntityOrThrow(id);
        MenuCategoryEntity menuCategory = request.menuCategoryId() != null
                ? getMenuCategoryOrThrow(request.menuCategoryId(), hotelId)
                : null;
        menuItemMapper.patchEntity(entity, request, menuCategory);
        return menuItemMapper.toResponse(menuItemRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public MenuItemResponse get(Long id) {
        return menuItemMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<MenuItemResponse> search(MenuItemSearchCriteria criteria, Pageable pageable) {
        Long hotelId = getCurrentHotelId();
        return menuItemRepository.findAll(MenuItemSpecification.withCriteria(hotelId, criteria), pageable)
                .map(menuItemMapper::toResponse);
    }

    @Transactional
    public void delete(Long id) {
        MenuItemEntity entity = getEntityOrThrow(id);
        deleteAllImages(entity);
        menuItemRepository.delete(entity);
    }

    @NoLogging
    @Transactional
    public MenuItemResponse uploadImage(Long id, MultipartFile file) {
        MenuItemEntity entity = getEntityOrThrow(id);
        FileUploadResponse upload;
        try {
            upload = fileLoadService.uploadFile(file, String.valueOf(id), MENU_ITEM_PHOTO_KEY);
        } catch (IOException e) {
            log.error("Menu item image upload failed for menuItemId={}", id, e);
            throw ApiExceptions.badRequest(MessageKey.BAD_REQUEST_FILE_UPLOAD_FAILED);
        }

        String imageUrl = upload.getUrl();
        entity.getPhotoUrls().add(imageUrl);
        if (entity.getMainImageUrl() == null || entity.getMainImageUrl().isEmpty()) {
            entity.setMainImageUrl(imageUrl);
        }
        return menuItemMapper.toResponse(menuItemRepository.save(entity));
    }

    @Transactional
    public MenuItemResponse deleteImage(Long id, String imageUrl) {
        MenuItemEntity entity = getEntityOrThrow(id);
        String targetImageUrl = findGalleryImage(entity, imageUrl);
        if (entity.getPhotoUrls().remove(targetImageUrl)) {
            if (targetImageUrl.equals(entity.getMainImageUrl())) {
                entity.setMainImageUrl(
                        entity.getPhotoUrls().isEmpty() ? null : entity.getPhotoUrls().iterator().next()
                );
            }
            menuItemRepository.save(entity);
            fileLoadService.deleteByPublicUrl(targetImageUrl);
        }
        return menuItemMapper.toResponse(entity);
    }

    @Transactional
    public MenuItemResponse setMainImage(Long id, String imageUrl) {
        MenuItemEntity entity = getEntityOrThrow(id);
        if (!entity.getPhotoUrls().contains(imageUrl)) {
            throw ApiExceptions.imageNotInGallery(EntityKey.MENU_ITEM, imageUrl);
        }
        entity.setMainImageUrl(imageUrl);
        return menuItemMapper.toResponse(menuItemRepository.save(entity));
    }

    private String findGalleryImage(MenuItemEntity entity, String imageUrl) {
        String targetKey = fileLoadService.resolveKey(imageUrl);
        return entity.getPhotoUrls().stream()
                .filter(photoUrl -> {
                    String photoKey = fileLoadService.resolveKey(photoUrl);
                    return photoUrl.equals(imageUrl) || (photoKey != null && photoKey.equals(targetKey));
                })
                .findFirst()
                .orElseThrow(() -> ApiExceptions.imageNotInGallery(EntityKey.MENU_ITEM, imageUrl));
    }

    private void deleteAllImages(MenuItemEntity entity) {
        new ArrayList<>(entity.getPhotoUrls()).forEach(fileLoadService::deleteByPublicUrl);
        entity.getPhotoUrls().clear();
        entity.setMainImageUrl(null);
    }

    private MenuItemEntity getEntityOrThrow(Long id) {
        Long hotelId = getCurrentHotelId();
        return menuItemRepository.findByIdAndHotelId(id, hotelId)
                .orElseThrow(() -> ApiExceptions.notFound(EntityKey.MENU_ITEM));
    }

    private MenuCategoryEntity getMenuCategoryOrThrow(Long menuCategoryId, Long hotelId) {
        return menuCategoryRepository.findByIdAndHotelId(menuCategoryId, hotelId)
                .orElseThrow(() -> ApiExceptions.notFound(EntityKey.MENU_CATEGORY));
    }
}
