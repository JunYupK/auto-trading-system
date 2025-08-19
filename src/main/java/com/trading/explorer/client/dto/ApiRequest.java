package com.trading.explorer.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiRequest {
    
    private String apiId;
    private String bizDate;
    private String format;
    private Map<String, String> additionalParams;
    
    public Map<String, String> toParameterMap() {
        Map<String, String> params = new HashMap<>();
        
        if (format != null) {
            params.put("format", format);
        }
        
        if (bizDate != null) {
            params.put("bizdate", bizDate);
        }
        
        if (additionalParams != null) {
            params.putAll(additionalParams);
        }
        
        return params;
    }
}