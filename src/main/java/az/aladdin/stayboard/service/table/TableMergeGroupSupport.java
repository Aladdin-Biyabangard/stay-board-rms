package az.aladdin.stayboard.service.table;

import az.aladdin.stayboard.entity.TableEntity;
import az.aladdin.stayboard.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TableMergeGroupSupport {

    private final TableRepository tableRepository;

    public Map<Long, List<Long>> buildTableGroupMap(List<TableEntity> tables) {
        Map<String, List<TableEntity>> grouped = new HashMap<>();
        for (TableEntity table : tables) {
            String key = table.getMergeGroupId() != null
                    ? "group:" + table.getMergeGroupId()
                    : "table:" + table.getId();
            grouped.computeIfAbsent(key, ignored -> new ArrayList<>()).add(table);
        }

        Map<Long, List<Long>> result = new HashMap<>();
        for (List<TableEntity> group : grouped.values()) {
            List<Long> ids = group.stream().map(TableEntity::getId).toList();
            for (TableEntity table : group) {
                result.put(table.getId(), ids);
            }
        }
        return result;
    }

    public List<Long> resolveGroupTableIds(Long hotelId, TableEntity table) {
        if (table.getMergeGroupId() == null) {
            return List.of(table.getId());
        }
        return tableRepository.findByMergeGroupIdAndHotelId(table.getMergeGroupId(), hotelId).stream()
                .map(TableEntity::getId)
                .toList();
    }
}
