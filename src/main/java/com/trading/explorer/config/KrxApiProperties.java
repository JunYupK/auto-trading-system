package com.trading.explorer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "krx")
public class KrxApiProperties {
    
    private Api api = new Api();
    private Map<String, List<ApiDefinition>> apis;
    
    @Data
    public static class Api {
        private String baseUrl = "https://data-api.krx.co.kr";
        private String key = "";
        private Duration timeout = Duration.ofSeconds(30);
        private String defaultFormat = "json";
    }
    
    @Data
    public static class ApiDefinition {
        private String id;
        private String name;
        private String category;
    }
}