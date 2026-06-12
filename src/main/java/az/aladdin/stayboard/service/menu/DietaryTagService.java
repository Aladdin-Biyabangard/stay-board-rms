package az.aladdin.stayboard.service.menu;

import az.aladdin.stayboard.entity.DietaryTagEntity;
import az.aladdin.stayboard.exception.ApiExceptions;
import az.aladdin.stayboard.exception.EntityKey;
import az.aladdin.stayboard.mapper.DietaryTagMapper;
import az.aladdin.stayboard.model.request.CreateDietaryTagRequest;
import az.aladdin.stayboard.model.request.PatchDietaryTagRequest;
import az.aladdin.stayboard.model.request.search.DietaryTagSearchCriteria;
import az.aladdin.stayboard.model.response.DietaryTagResponse;
import az.aladdin.stayboard.repository.DietaryTagRepository;
import az.aladdin.stayboard.service.hotel.HotelAwareService;
import az.aladdin.stayboard.specification.DietaryTagSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DietaryTagService extends HotelAwareService {

    private final DietaryTagRepository dietaryTagRepository;
    private final DietaryTagMapper dietaryTagMapper;

    @Transactional
    public DietaryTagResponse create(CreateDietaryTagRequest request) {
        Long hotelId = getCurrentHotelId();
        DietaryTagEntity entity = dietaryTagMapper.toEntity(request, hotelId);
        return dietaryTagMapper.toResponse(dietaryTagRepository.save(entity));
    }

    @Transactional
    public DietaryTagResponse update(Long id, CreateDietaryTagRequest request) {
        DietaryTagEntity entity = getEntityOrThrow(id);
        dietaryTagMapper.updateEntity(entity, request);
        return dietaryTagMapper.toResponse(dietaryTagRepository.save(entity));
    }

    @Transactional
    public DietaryTagResponse patch(Long id, PatchDietaryTagRequest request) {
        DietaryTagEntity entity = getEntityOrThrow(id);
        dietaryTagMapper.patchEntity(entity, request);
        return dietaryTagMapper.toResponse(dietaryTagRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public DietaryTagResponse get(Long id) {
        return dietaryTagMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<DietaryTagResponse> search(DietaryTagSearchCriteria criteria, Pageable pageable) {
        Long hotelId = getCurrentHotelId();
        return dietaryTagRepository.findAll(DietaryTagSpecification.withCriteria(hotelId, criteria), pageable)
                .map(dietaryTagMapper::toResponse);
    }

    @Transactional
    public void delete(Long id) {
        dietaryTagRepository.delete(getEntityOrThrow(id));
    }

    private DietaryTagEntity getEntityOrThrow(Long id) {
        Long hotelId = getCurrentHotelId();
        return dietaryTagRepository.findByIdAndHotelId(id, hotelId)
                .orElseThrow(() -> ApiExceptions.notFound(EntityKey.DIETARY_TAG));
    }
}
