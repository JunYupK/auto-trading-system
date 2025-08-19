package com.trading.explorer.controller;

import com.trading.explorer.client.dto.ApiResponse;
import com.trading.explorer.config.KrxApiProperties;
import com.trading.explorer.service.ApiExplorerService;
import com.trading.explorer.util.JsonFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/explore")
@RequiredArgsConstructor
public class ApiExplorerController {
    
    private final ApiExplorerService apiExplorerService;
    private final JsonFormatter jsonFormatter;
    private final KrxApiProperties properties;
    
    @GetMapping("/all")
    public Mono<Map<String, Object>> exploreAllApis() {
        log.info("Request received: explore all APIs");
        return apiExplorerService.exploreAllApis();
    }
    
    @GetMapping("/category/{category}")
    public Mono<Map<String, Object>> exploreByCategory(@PathVariable String category) {
        log.info("Request received: explore category {}", category);
        return apiExplorerService.exploreByCategory(category);
    }
    
    @GetMapping(value = "/single/{apiId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> exploreSingleApi(
            @PathVariable String apiId,
            @RequestParam(required = false) String bizDate) {
        
        log.info("Request received: explore single API {} with date {}", apiId, bizDate);
        
        return apiExplorerService.exploreSingleApi(apiId, bizDate)
                .map(response -> {
                    Map<String, Object> result = Map.of(
                        "apiId", apiId,
                        "success", response.isSuccess(),
                        "httpStatus", response.getHttpStatus(),
                        "responseTimeMs", response.getResponseTimeMs(),
                        "timestamp", response.getTimestamp(),
                        "response", response.isSuccess() ? 
                            jsonFormatter.prettyPrint(response.getRawResponse()) : null,
                        "errorMessage", response.getErrorMessage(),
                        "structure", response.isSuccess() ? 
                            jsonFormatter.analyzeStructure(response.getRawResponse()) : null
                    );
                    
                    return result;
                });
    }
    
    @GetMapping(value = "/single/{apiId}/raw", produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<String> exploreSingleApiRaw(
            @PathVariable String apiId,
            @RequestParam(required = false) String bizDate) {
        
        log.info("Request received: explore single API {} raw response with date {}", apiId, bizDate);
        
        return apiExplorerService.exploreSingleApi(apiId, bizDate)
                .map(response -> response.isSuccess() ? 
                    response.getRawResponse() : 
                    "ERROR: " + response.getErrorMessage());
    }
    
    @GetMapping("/categories")
    public Mono<Map<String, Object>> getAvailableCategories() {
        log.info("Request received: get available categories");
        
        return Mono.just(Map.of(
            "categories", properties.getApis().keySet(),
            "totalCategories", properties.getApis().size(),
            "apis", properties.getApis()
        ));
    }
    
    @GetMapping("/health")
    public Mono<Map<String, Object>> healthCheck() {
        return Mono.just(Map.of(
            "status", "UP",
            "service", "KRX API Explorer",
            "timestamp", System.currentTimeMillis()
        ));
    }
}