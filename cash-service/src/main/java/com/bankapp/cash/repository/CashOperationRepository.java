package com.bankapp.cash.repository;

import com.bankapp.cash.entity.CashOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CashOperationRepository extends JpaRepository<CashOperation, Long> {
    
    /**
     * Найти операцию по sagaId (для Choreography Saga Pattern).
     */
    Optional<CashOperation> findBySagaId(String sagaId);
}
