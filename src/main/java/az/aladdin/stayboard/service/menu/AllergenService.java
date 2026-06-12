package az.aladdin.stayboard.service.menu;

import az.aladdin.stayboard.entity.AllergenEntity;
import az.aladdin.stayboard.exception.EntityKey;
import az.aladdin.stayboard.mapper.AllergenMapper;
import az.aladdin.stayboard.model.request.CreateAllergenRequest;
import az.aladdin.stayboard.model.request.PatchAllergenRequest;
import az.aladdin.stayboard.model.request.search.AllergenSearchCriteria;
import az.aladdin.stayboard.model.response.AllergenResponse;
import az.aladdin.stayboard.repository.AllergenRepository;
import az.aladdin.stayboard.service.hotel.HotelAwareService;
import az.aladdin.stayboard.specification.AllergenSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AllergenService extends HotelAwareService {

    private final AllergenRepository allergenRepository;
    private final AllergenMapper allergenMapper;

    @Transactional
    public AllergenResponse create(CreateAllergenRequest request) {
        Long hotelId = getCurrentHotelId();
        AllergenEntity entity = allergenMapper.toEntity(request, hotelId);
        return allergenMapper.toResponse(allergenRepository.save(entity));
    }

    @Transactional
    public AllergenResponse update(Long id, CreateAllergenRequest request) {
        AllergenEntity entity = getEntityOrThrow(id);
        allergenMapper.updateEntity(entity, request);
        return allergenMapper.toResponse(allergenRepository.save(entity));
    }

    @Transactional
    public AllergenResponse patch(Long id, PatchAllergenRequest request) {
        AllergenEntity entity = getEntityOrThrow(id);
        allergenMapper.patchEntity(entity, request);
        return allergenMapper.toResponse(allergenRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public AllergenResponse get(Long id) {
        return allergenMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<AllergenResponse> search(AllergenSearchCriteria criteria, Pageable pageable) {
        Long hotelId = getCurrentHotelId();
        return allergenRepository.findAll(AllergenSpecification.withCriteria(hotelId, criteria), pageable)
                .map(allergenMapper::toResponse);
    }

    @Transactional
    public void delete(Long id) {
        allergenRepository.delete(getEntityOrThrow(id));
    }

    private AllergenEntity getEntityOrThrow(Long id) {
        return requireEntity(
                allergenRepository.findByIdAndHotelId(id, getCurrentHotelId()),
                EntityKey.ALLERGEN
        );
    }
}
