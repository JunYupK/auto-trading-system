package com.trading.explorer.service;

import com.trading.explorer.client.KrxApiClient;
import com.trading.explorer.client.dto.ApiResponse;
import com.trading.explorer.config.KrxApiProperties;
import com.trading.explorer.util.JsonFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiExplorerService {
    
    private final KrxApiClient krxApiClient;
    private final KrxApiProperties properties;
    private final JsonFormatter jsonFormatter;
    
    public Mono<Map<String, Object>> exploreAllApis() {
        log.info("Starting exploration of all KRX APIs");
        
        List<KrxApiProperties.ApiDefinition> allApis = properties.getApis().values()
                .stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
        
        return Flux.fromIterable(allApis)
                .delayElements(Duration.ofMillis(500)) // API 호출 간격 조절
                .flatMap(api -> {
                    log.debug("Exploring API: {} ({})", api.getId(), api.getName());
                    return krxApiClient.callApiWithDefaultDate(api.getId())
                            .map(response -> Map.entry(api.getId(), createApiResult(api, response)))
                            .doOnNext(entry -> {
                                if (entry.getValue() instanceof Map<?, ?> result) {
                                    log.info("API {} exploration completed: success={}", 
                                            entry.getKey(), result.get("success"));
                                }
                            });
                })
                .collectMap(Map.Entry::getKey, Map.Entry::getValue)
                .map(results -> {
                    Map<String, Object> summary = new HashMap<>();
                    summary.put("totalApis", allApis.size());
                    summary.put("results", results);
                    summary.put("summary", createSummary(results));
                    
                    log.info("All APIs exploration completed. Total: {}", allApis.size());
                    return summary;
                });
    }
    
    public Mono<Map<String, Object>> exploreByCategory(String category) {
        log.info("Exploring APIs for category: {}", category);
        
        List<KrxApiProperties.ApiDefinition> categoryApis = properties.getApis().get(category);
        
        if (categoryApis == null || categoryApis.isEmpty()) {
            return Mono.just(Map.of(
                "error", "Category not found or empty: " + category,
                "availableCategories", properties.getApis().keySet()
            ));
        }
        
        return Flux.fromIterable(categoryApis)
                .delayElements(Duration.ofMillis(300))
                .flatMap(api -> {
                    log.debug("Exploring API: {} ({})", api.getId(), api.getName());
                    return krxApiClient.callApiWithDefaultDate(api.getId())
                            .map(response -> Map.entry(api.getId(), createApiResult(api, response)));
                })
                .collectMap(Map.Entry::getKey, Map.Entry::getValue)
                .map(results -> {
                    Map<String, Object> categoryResult = new HashMap<>();
                    categoryResult.put("category", category);
                    categoryResult.put("totalApis", categoryApis.size());
                    categoryResult.put("results", results);
                    categoryResult.put("summary", createSummary(results));
                    
                    log.info("Category {} exploration completed. APIs: {}", category, categoryApis.size());
                    return categoryResult;
                });
    }
    
    public Mono<ApiResponse> exploreSingleApi(String apiId, String bizDate) {
        log.info("Exploring single API: {} with date: {}", apiId, bizDate);
        
        if (bizDate != null && !bizDate.isEmpty()) {
            return krxApiClient.callApiWithDate(apiId, bizDate)
                    .doOnNext(response -> {
                        if (response.isSuccess()) {
                            jsonFormatter.saveToFile(apiId, response.getRawResponse());
                        }
                    });
        } else {
            return krxApiClient.callApiWithDefaultDate(apiId)
                    .doOnNext(response -> {
                        if (response.isSuccess()) {
                            jsonFormatter.saveToFile(apiId, response.getRawResponse());
                        }
                    });
        }
    }
    
    private Map<String, Object> createApiResult(KrxApiProperties.ApiDefinition api, ApiResponse response) {
        Map<String, Object> result = new HashMap<>();
        result.put("apiId", api.getId());
        result.put("name", api.getName());
        result.put("category", api.getCategory());
        result.put("success", response.isSuccess());
        result.put("httpStatus", response.getHttpStatus());
        result.put("responseTimeMs", response.getResponseTimeMs());
        result.put("timestamp", response.getTimestamp());
        
        if (response.isSuccess()) {
            result.put("responseSize", response.getRawResponse().length());
            result.put("structure", jsonFormatter.analyzeStructure(response.getRawResponse()));
            
            // 응답을 파일로 저장
            jsonFormatter.saveToFile(api.getId(), response.getRawResponse());
        } else {
            result.put("errorMessage", response.getErrorMessage());
        }
        
        return result;
    }
    
    private Map<String, Object> createSummary(Map<String, Object> results) {
        long successCount = results.values().stream()
                .mapToLong(result -> {
                    if (result instanceof Map<?, ?> map) {
                        return Boolean.TRUE.equals(map.get("success")) ? 1L : 0L;
                    }
                    return 0L;
                })
                .sum();
        
        long failureCount = results.size() - successCount;
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("total", results.size());
        summary.put("success", successCount);
        summary.put("failure", failureCount);
        summary.put("successRate", results.size() > 0 ? (double) successCount / results.size() : 0.0);
        
        return summary;
    }
}