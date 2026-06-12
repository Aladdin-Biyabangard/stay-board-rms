package az.aladdin.stayboard.service.menu;

import az.aladdin.stayboard.entity.ModifierGroupEntity;
import az.aladdin.stayboard.exception.ApiExceptions;
import az.aladdin.stayboard.exception.EntityKey;
import az.aladdin.stayboard.exception.MessageKey;
import az.aladdin.stayboard.mapper.ModifierGroupMapper;
import az.aladdin.stayboard.model.request.CreateModifierGroupRequest;
import az.aladdin.stayboard.model.request.PatchModifierGroupRequest;
import az.aladdin.stayboard.model.request.search.ModifierGroupSearchCriteria;
import az.aladdin.stayboard.model.response.ModifierGroupResponse;
import az.aladdin.stayboard.repository.MenuItemModifierGroupRepository;
import az.aladdin.stayboard.repository.ModifierGroupRepository;
import az.aladdin.stayboard.service.hotel.HotelAwareService;
import az.aladdin.stayboard.specification.ModifierGroupSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ModifierGroupService extends HotelAwareService {

    private final ModifierGroupRepository modifierGroupRepository;
    private final MenuItemModifierGroupRepository menuItemModifierGroupRepository;
    private final ModifierGroupMapper modifierGroupMapper;

    @Transactional
    public ModifierGroupResponse create(CreateModifierGroupRequest request) {
        Long hotelId = getCurrentHotelId();
        ModifierGroupEntity entity = modifierGroupMapper.toEntity(request, hotelId);
        return modifierGroupMapper.toResponse(modifierGroupRepository.save(entity));
    }

    @Transactional
    public ModifierGroupResponse update(Long id, CreateModifierGroupRequest request) {
        ModifierGroupEntity entity = getEntityOrThrow(id);
        modifierGroupMapper.updateEntity(entity, request);
        return modifierGroupMapper.toResponse(modifierGroupRepository.save(entity));
    }

    @Transactional
    public ModifierGroupResponse patch(Long id, PatchModifierGroupRequest request) {
        ModifierGroupEntity entity = getEntityOrThrow(id);
        modifierGroupMapper.patchEntity(entity, request);
        return modifierGroupMapper.toResponse(modifierGroupRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public ModifierGroupResponse get(Long id) {
        return modifierGroupMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<ModifierGroupResponse> search(ModifierGroupSearchCriteria criteria, Pageable pageable) {
        Long hotelId = getCurrentHotelId();
        Page<ModifierGroupEntity> entityPage = modifierGroupRepository.findAll(
                ModifierGroupSpecification.withCriteria(hotelId, criteria),
                pageable
        );
        var content = entityPage.getContent().stream()
                .map(modifierGroupMapper::toResponse)
                .toList();
        return new PageImpl<>(content, pageable, entityPage.getTotalElements());
    }

    @Transactional
    public void delete(Long id) {
        ModifierGroupEntity entity = getEntityOrThrow(id);
        if (menuItemModifierGroupRepository.existsByModifierGroup_IdAndHotelId(entity.getId(), entity.getHotelId())) {
            throw ApiExceptions.conflict(MessageKey.CONFLICT_MODIFIER_GROUP_IN_USE);
        }
        modifierGroupRepository.delete(entity);
    }

    private ModifierGroupEntity getEntityOrThrow(Long id) {
        Long hotelId = getCurrentHotelId();
        return modifierGroupRepository.findByIdAndHotelId(id, hotelId)
                .orElseThrow(() -> ApiExceptions.notFound(EntityKey.MODIFIER_GROUP));
    }
}
