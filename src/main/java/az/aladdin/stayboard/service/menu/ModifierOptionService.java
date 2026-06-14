package az.aladdin.stayboard.service.menu;

import az.aladdin.stayboard.entity.ModifierGroupEntity;
import az.aladdin.stayboard.entity.ModifierOptionEntity;
import az.aladdin.stayboard.exception.ApiExceptions;
import az.aladdin.stayboard.exception.EntityKey;
import az.aladdin.stayboard.mapper.ModifierOptionMapper;
import az.aladdin.stayboard.model.request.CreateModifierOptionRequest;
import az.aladdin.stayboard.model.request.PatchModifierOptionRequest;
import az.aladdin.stayboard.model.request.search.ModifierOptionSearchCriteria;
import az.aladdin.stayboard.model.response.ModifierOptionResponse;
import az.aladdin.stayboard.repository.ModifierGroupRepository;
import az.aladdin.stayboard.repository.ModifierOptionRepository;
import az.aladdin.stayboard.service.hotel.HotelAwareService;
import az.aladdin.stayboard.specification.ModifierOptionSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModifierOptionService extends HotelAwareService {

    private final ModifierOptionRepository modifierOptionRepository;
    private final ModifierGroupRepository modifierGroupRepository;
    private final ModifierOptionMapper modifierOptionMapper;

    @Transactional
    public ModifierOptionResponse create(CreateModifierOptionRequest request) {
        Long hotelId = getCurrentHotelId();
        ModifierGroupEntity group = getGroupOrThrow(request.modifierGroupId(), hotelId);
        ModifierOptionEntity entity = modifierOptionMapper.toEntity(request, hotelId, group);
        return modifierOptionMapper.toResponse(modifierOptionRepository.save(entity));
    }

    @Transactional
    public ModifierOptionResponse update(Long id, CreateModifierOptionRequest request) {
        Long hotelId = getCurrentHotelId();
        ModifierOptionEntity entity = getEntityOrThrow(id);
        ModifierGroupEntity group = getGroupOrThrow(request.modifierGroupId(), hotelId);
        modifierOptionMapper.updateEntity(entity, request, group);
        return modifierOptionMapper.toResponse(modifierOptionRepository.save(entity));
    }

    @Transactional
    public ModifierOptionResponse patch(Long id, PatchModifierOptionRequest request) {
        Long hotelId = getCurrentHotelId();
        ModifierOptionEntity entity = getEntityOrThrow(id);
        ModifierGroupEntity group = request.modifierGroupId() != null
                ? getGroupOrThrow(request.modifierGroupId(), hotelId)
                : null;
        modifierOptionMapper.patchEntity(entity, request, group);
        return modifierOptionMapper.toResponse(modifierOptionRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public ModifierOptionResponse get(Long id) {
        return modifierOptionMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<ModifierOptionResponse> search(ModifierOptionSearchCriteria criteria, Pageable pageable) {
        Long hotelId = getCurrentHotelId();
        Page<ModifierOptionEntity> entityPage = modifierOptionRepository.findAll(
                ModifierOptionSpecification.withCriteria(hotelId, criteria),
                pageable
        );
        var content = entityPage.getContent().stream()
                .map(modifierOptionMapper::toResponse)
                .toList();
        return new PageImpl<>(content, pageable, entityPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<ModifierOptionResponse> listActiveByGroupId(Long modifierGroupId) {
        Long hotelId = getCurrentHotelId();
        return modifierOptionRepository.findByModifierGroup_IdAndHotelIdOrderBySortOrderAsc(modifierGroupId, hotelId)
                .stream()
                .filter(ModifierOptionEntity::isActive)
                .map(modifierOptionMapper::toResponse)
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        modifierOptionRepository.delete(getEntityOrThrow(id));
    }

    private ModifierOptionEntity getEntityOrThrow(Long id) {
        Long hotelId = getCurrentHotelId();
        return modifierOptionRepository.findByIdAndHotelId(id, hotelId)
                .orElseThrow(() -> ApiExceptions.notFound(EntityKey.MODIFIER_OPTION));
    }

    private ModifierGroupEntity getGroupOrThrow(Long groupId, Long hotelId) {
        return modifierGroupRepository.findByIdAndHotelId(groupId, hotelId)
                .orElseThrow(() -> ApiExceptions.notFound(EntityKey.MODIFIER_GROUP));
    }
}
