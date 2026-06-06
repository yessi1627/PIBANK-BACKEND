package com.pibank.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private String errorCode;
    private T data;
    private Map<String, String> validationErrors;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
            .success(true)
            .message(message)
            .data(data)
            .build();
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Operación exitosa");
    }

    public static ApiResponse<Void> successMessage(String message) {
        return ApiResponse.<Void>builder()
            .success(true)
            .message(message)
            .build();
    }

    public static ApiResponse<Void> error(String message, String errorCode) {
        return ApiResponse.<Void>builder()
            .success(false)
            .message(message)
            .errorCode(errorCode)
            .build();
    }

    public static <T> ApiResponse<T> validationError(String message, Map<String, String> errors) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .errorCode("VALIDATION_ERROR")
            .validationErrors(errors)
            .build();
    }
}
