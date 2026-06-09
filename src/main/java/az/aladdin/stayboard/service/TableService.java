package az.aladdin.stayboard.service;

import az.aladdin.stayboard.service.hotel.HotelAwareService;
import az.aladdin.stayboard.entity.TableEntity;
import az.aladdin.stayboard.exception.ApiExceptions;
import az.aladdin.stayboard.exception.EntityKey;
import az.aladdin.stayboard.exception.MessageKey;
import az.aladdin.stayboard.mapper.TableMapper;
import az.aladdin.stayboard.model.request.CreateTableRequest;
import az.aladdin.stayboard.model.request.PatchTableRequest;
import az.aladdin.stayboard.model.request.UpdateTableRequest;
import az.aladdin.stayboard.model.request.search.TableSearchCriteria;
import az.aladdin.stayboard.model.response.TableResponse;
import az.aladdin.stayboard.repository.OrderRepository;
import az.aladdin.stayboard.repository.TableRepository;
import az.aladdin.stayboard.specification.TableSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TableService extends HotelAwareService {

    private final TableRepository tableRepository;
    private final OrderRepository orderRepository;
    private final TableMapper tableMapper;

    @Transactional
    public TableResponse create(CreateTableRequest request) {
        Long hotelId = getCurrentHotelId();
        TableEntity entity = tableMapper.toEntity(request, hotelId);
        return tableMapper.toResponse(tableRepository.save(entity));
    }

    @Transactional
    public TableResponse update(Long id, UpdateTableRequest request) {
        TableEntity entity = getEntityOrThrow(id);
        tableMapper.updateEntity(entity, request);
        return tableMapper.toResponse(tableRepository.save(entity));
    }

    @Transactional
    public TableResponse patch(Long id, PatchTableRequest request) {
        TableEntity entity = getEntityOrThrow(id);
        tableMapper.patchEntity(entity, request);
        return tableMapper.toResponse(tableRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public TableResponse get(Long id) {
        return tableMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<TableResponse> search(TableSearchCriteria criteria, Pageable pageable) {
        Long hotelId = getCurrentHotelId();
        return tableRepository.findAll(TableSpecification.withCriteria(hotelId, criteria), pageable)
                .map(tableMapper::toResponse);
    }

    @Transactional
    public void delete(Long id) {
        TableEntity entity = getEntityOrThrow(id);
        if (entity.getMergeGroupId() != null) {
            throw ApiExceptions.conflict(MessageKey.CONFLICT_TABLE_MERGED);
        }
        if (orderRepository.existsByTableEntityIdAndHotelId(entity.getId(), entity.getHotelId())) {
            throw ApiExceptions.conflict(MessageKey.CONFLICT_TABLE_HAS_ORDERS);
        }
        tableRepository.delete(entity);
    }

    private TableEntity getEntityOrThrow(Long id) {
        Long hotelId = getCurrentHotelId();
        return tableRepository.findByIdAndHotelId(id, hotelId)
                .orElseThrow(() -> ApiExceptions.notFound(EntityKey.TABLE));
    }
}
