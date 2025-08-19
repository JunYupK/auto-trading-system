package com.trading.explorer.client;

import com.trading.explorer.client.dto.ApiRequest;
import com.trading.explorer.client.dto.ApiResponse;
import com.trading.explorer.config.KrxApiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class KrxApiClient {
    
    private final WebClient webClient;
    private final KrxApiProperties properties;
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    public Mono<ApiResponse> callApi(String apiId, Map<String, String> parameters) {
        long startTime = System.currentTimeMillis();
        
        ApiRequest request = ApiRequest.builder()
                .apiId(apiId)
                .format(properties.getApi().getDefaultFormat())
                .additionalParams(parameters != null ? parameters : new HashMap<>())
                .build();
        
        String uri = "/api/" + apiId;
        
        log.debug("Calling KRX API: {} with parameters: {}", uri, request.toParameterMap());
        
        return webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path(uri);
                    
                    // API Key가 있으면 헤더에 추가
                    if (properties.getApi().getKey() != null && !properties.getApi().getKey().isEmpty()) {
                        // KRX API 키 방식에 따라 조정 필요
                        request.getAdditionalParams().put("key", properties.getApi().getKey());
                    }
                    
                    // 쿼리 파라미터 추가
                    request.toParameterMap().forEach(builder::queryParam);
                    
                    return builder.build();
                })
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> 
                    response.bodyToMono(String.class)
                        .map(errorBody -> new WebClientResponseException(
                            response.statusCode().value(),
                            "KRX API Error",
                            null,
                            errorBody.getBytes(),
                            null
                        ))
                )
                .bodyToMono(String.class)
                .map(responseBody -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    log.debug("KRX API response received for {}: {} chars in {}ms", 
                             apiId, responseBody.length(), responseTime);
                    return ApiResponse.success(apiId, responseBody, responseTime);
                })
                .onErrorResume(WebClientResponseException.class, ex -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    String errorBody = ex.getResponseBodyAsString();
                    log.error("KRX API error for {}: {} - {}", apiId, ex.getStatusCode(), errorBody);
                    return Mono.just(ApiResponse.failure(apiId, 
                        "HTTP " + ex.getStatusCode() + ": " + errorBody, 
                        ex.getStatusCode().value(), responseTime));
                })
                .onErrorResume(Exception.class, ex -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    log.error("Unexpected error calling KRX API {}: ", apiId, ex);
                    return Mono.just(ApiResponse.failure(apiId, 
                        "Unexpected error: " + ex.getMessage(), 
                        500, responseTime));
                });
    }
    
    public Mono<ApiResponse> callApi(String apiId) {
        return callApi(apiId, new HashMap<>());
    }
    
    public Mono<ApiResponse> callApiWithDefaultDate(String apiId) {
        // 어제 날짜 사용 (오늘 데이터가 없을 가능성이 높음)
        String yesterday = LocalDate.now().minusDays(1).format(DATE_FORMAT);
        
        Map<String, String> params = new HashMap<>();
        params.put("bizdate", yesterday);
        
        return callApi(apiId, params);
    }
    
    public Mono<ApiResponse> callApiWithDate(String apiId, String bizDate) {
        Map<String, String> params = new HashMap<>();
        params.put("bizdate", bizDate);
        
        return callApi(apiId, params);
    }
}