package com.naratrad.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard success response DTO
 * Used for simple success messages
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SuccessResponse {
    private String message;
}