package com.trading.explorer.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {
    
    private String apiId;
    private String rawResponse;
    private boolean success;
    private String errorMessage;
    private LocalDateTime timestamp;
    private long responseTimeMs;
    private int httpStatus;
    
    public static ApiResponse success(String apiId, String rawResponse, long responseTimeMs) {
        return ApiResponse.builder()
                .apiId(apiId)
                .rawResponse(rawResponse)
                .success(true)
                .timestamp(LocalDateTime.now())
                .responseTimeMs(responseTimeMs)
                .httpStatus(200)
                .build();
    }
    
    public static ApiResponse failure(String apiId, String errorMessage, int httpStatus, long responseTimeMs) {
        return ApiResponse.builder()
                .apiId(apiId)
                .success(false)
                .errorMessage(errorMessage)
                .timestamp(LocalDateTime.now())
                .responseTimeMs(responseTimeMs)
                .httpStatus(httpStatus)
                .build();
    }
}