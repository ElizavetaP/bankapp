package com.bankapp.cash.repository;

import com.bankapp.cash.entity.CashOperation;
import com.bankapp.cash.entity.OperationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CashOperationRepository extends JpaRepository<CashOperation, Long> {
    
    List<CashOperation> findByUserLoginOrderByCreatedAtDesc(String userLogin);
    
    List<CashOperation> findByUserLoginAndOperationType(String userLogin, OperationType operationType);
    
    /**
     * Найти операцию по sagaId (для Choreography Saga Pattern).
     */
    Optional<CashOperation> findBySagaId(String sagaId);
}
