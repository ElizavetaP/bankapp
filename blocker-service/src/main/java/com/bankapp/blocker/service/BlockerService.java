package com.bankapp.blocker.service;

import com.bankapp.blocker.dto.BlockOperationRequest;
import com.bankapp.blocker.entity.BlockedOperation;
import com.bankapp.blocker.entity.OperationType;
import com.bankapp.blocker.repository.BlockedOperationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlockerService {

    private final BlockedOperationRepository blockedOperationRepository;

    /**
     * Заблокировать операцию для пользователя.
     */
    @Transactional
    public BlockedOperation blockOperation(BlockOperationRequest request) {
        log.info("Blocking operation: user={}, type={}", 
                request.getUserLogin(), request.getOperationType());

        // Проверяем, не заблокирована ли уже
        if (isOperationBlocked(request.getUserLogin(), request.getOperationType())) {
            throw new IllegalArgumentException("Operation already blocked");
        }

        BlockedOperation blockedOperation = BlockedOperation.builder()
                .userLogin(request.getUserLogin())
                .operationType(request.getOperationType())
                .reason(request.getReason())
                .build();

        BlockedOperation saved = blockedOperationRepository.save(blockedOperation);
        log.info("Operation blocked: id={}", saved.getId());

        return saved;
    }

    /**
     * Проверить, заблокирована ли операция для пользователя.
     */
    @Transactional(readOnly = true)
    public boolean isOperationBlocked(String userLogin, OperationType operationType) {
        boolean blocked = blockedOperationRepository
                .existsByUserLoginAndOperationType(userLogin, operationType);
        log.debug("Check blocked: user={}, type={}, result={}", userLogin, operationType, blocked);
        return blocked;
    }
}


