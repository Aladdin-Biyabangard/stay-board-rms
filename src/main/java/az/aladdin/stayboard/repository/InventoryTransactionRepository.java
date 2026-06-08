package az.aladdin.stayboard.repository;

import az.aladdin.stayboard.entity.InventoryTransactionEntity;
import az.aladdin.stayboard.model.enums.InventoryTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransactionEntity, Long>, JpaSpecificationExecutor<InventoryTransactionEntity> {

    boolean existsByReferenceIdAndReferenceTypeAndTransactionType(
            Long referenceId,
            String referenceType,
            InventoryTransactionType transactionType
    );

    List<InventoryTransactionEntity> findByReferenceIdAndReferenceTypeAndTransactionType(
            Long referenceId,
            String referenceType,
            InventoryTransactionType transactionType
    );
}
