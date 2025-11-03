package com.bankapp.blocker.repository;

import com.bankapp.blocker.entity.BlockedOperation;
import com.bankapp.blocker.entity.OperationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlockedOperationRepository extends JpaRepository<BlockedOperation, Long> {
    
    /**
     * Проверить, существует ли блокировка.
     */
    boolean existsByUserLoginAndOperationType(String userLogin, OperationType operationType);
}
