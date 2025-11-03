package com.bankapp.blocker.controller;

import com.bankapp.blocker.dto.BlockOperationRequest;
import com.bankapp.blocker.entity.BlockedOperation;
import com.bankapp.blocker.entity.OperationType;
import com.bankapp.blocker.service.BlockerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/blocker")
@RequiredArgsConstructor
@Slf4j
public class BlockerController {

    private final BlockerService blockerService;

    /**
     * Заблокировать операцию.
     */
    @PostMapping("/block")
    public ResponseEntity<BlockedOperation> blockOperation(@Valid @RequestBody BlockOperationRequest request) {
        try {
            BlockedOperation blocked = blockerService.blockOperation(request);
            return ResponseEntity.ok(blocked);
        } catch (IllegalArgumentException e) {
            log.error("Failed to block operation: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Проверить, заблокирована ли операция.
     */
    @GetMapping("/check")
    public ResponseEntity<Boolean> isOperationBlocked(
            @RequestParam String userLogin,
            @RequestParam OperationType operationType) {
        boolean blocked = blockerService.isOperationBlocked(userLogin, operationType);
        return ResponseEntity.ok(blocked);
    }
}


