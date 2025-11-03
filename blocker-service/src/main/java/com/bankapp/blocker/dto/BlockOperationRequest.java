package com.bankapp.blocker.dto;

import com.bankapp.blocker.entity.OperationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlockOperationRequest {

    @NotBlank(message = "User login is required")
    private String userLogin;

    @NotNull(message = "Operation type is required")
    private OperationType operationType;

    @NotBlank(message = "Reason is required")
    private String reason;
}


