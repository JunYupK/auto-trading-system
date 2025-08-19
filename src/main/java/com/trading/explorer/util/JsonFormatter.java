package com.trading.explorer.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component
public class JsonFormatter {
    
    private final ObjectMapper objectMapper;
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    
    public JsonFormatter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }
    
    public String prettyPrint(String jsonString) {
        try {
            Object jsonObject = objectMapper.readValue(jsonString, Object.class);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
        } catch (JsonProcessingException e) {
            log.warn("Failed to pretty print JSON, returning original: {}", e.getMessage());
            return jsonString;
        }
    }
    
    public void saveToFile(String apiId, String response) {
        try {
            // logs/responses 디렉터리 생성
            Path responseDir = Paths.get("logs", "responses");
            Files.createDirectories(responseDir);
            
            // 파일명 생성: {apiId}_{timestamp}.json
            String timestamp = LocalDateTime.now().format(FILE_DATE_FORMAT);
            String filename = String.format("%s_%s.json", apiId, timestamp);
            Path filePath = responseDir.resolve(filename);
            
            // Pretty formatted JSON으로 저장
            String prettyJson = prettyPrint(response);
            Files.write(filePath, prettyJson.getBytes());
            
            log.debug("API response saved to file: {}", filePath);
        } catch (IOException e) {
            log.error("Failed to save API response to file for {}: {}", apiId, e.getMessage());
        }
    }
    
    public Map<String, Object> analyzeStructure(String jsonString) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonString);
            
            Map<String, Object> analysis = new HashMap<>();
            analysis.put("type", getNodeType(rootNode));
            analysis.put("fields", analyzeFields(rootNode));
            analysis.put("sampleSize", getSampleSize(rootNode));
            analysis.put("depth", calculateDepth(rootNode));
            
            return analysis;
        } catch (JsonProcessingException e) {
            log.warn("Failed to analyze JSON structure: {}", e.getMessage());
            return Map.of("error", "Invalid JSON format: " + e.getMessage());
        }
    }
    
    private String getNodeType(JsonNode node) {
        if (node.isObject()) return "object";
        if (node.isArray()) return "array";
        if (node.isTextual()) return "string";
        if (node.isNumber()) return "number";
        if (node.isBoolean()) return "boolean";
        if (node.isNull()) return "null";
        return "unknown";
    }
    
    private Map<String, Object> analyzeFields(JsonNode node) {
        Map<String, Object> fields = new HashMap<>();
        
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fieldIterator = node.fields();
            while (fieldIterator.hasNext()) {
                Map.Entry<String, JsonNode> field = fieldIterator.next();
                String fieldName = field.getKey();
                JsonNode fieldValue = field.getValue();
                
                Map<String, Object> fieldInfo = new HashMap<>();
                fieldInfo.put("type", getNodeType(fieldValue));
                
                if (fieldValue.isTextual()) {
                    String textValue = fieldValue.asText();
                    fieldInfo.put("sampleValue", textValue.length() > 50 ? 
                        textValue.substring(0, 50) + "..." : textValue);
                    fieldInfo.put("length", textValue.length());
                } else if (fieldValue.isNumber()) {
                    fieldInfo.put("sampleValue", fieldValue.asText());
                } else if (fieldValue.isArray()) {
                    fieldInfo.put("arraySize", fieldValue.size());
                    if (fieldValue.size() > 0) {
                        fieldInfo.put("elementType", getNodeType(fieldValue.get(0)));
                        if (fieldValue.get(0).isObject()) {
                            fieldInfo.put("elementFields", analyzeFields(fieldValue.get(0)));
                        }
                    }
                } else if (fieldValue.isObject()) {
                    fieldInfo.put("nestedFields", analyzeFields(fieldValue));
                }
                
                fields.put(fieldName, fieldInfo);
            }
        } else if (node.isArray()) {
            fields.put("arrayInfo", Map.of(
                "size", node.size(),
                "elementType", node.size() > 0 ? getNodeType(node.get(0)) : "empty",
                "firstElementFields", node.size() > 0 && node.get(0).isObject() ? 
                    analyzeFields(node.get(0)) : "N/A"
            ));
        }
        
        return fields;
    }
    
    private int getSampleSize(JsonNode node) {
        if (node.isArray()) {
            return node.size();
        } else if (node.isObject()) {
            return node.size(); // field count
        }
        return 1;
    }
    
    private int calculateDepth(JsonNode node) {
        if (node.isObject()) {
            int maxChildDepth = 0;
            Iterator<JsonNode> elements = node.elements();
            while (elements.hasNext()) {
                maxChildDepth = Math.max(maxChildDepth, calculateDepth(elements.next()));
            }
            return 1 + maxChildDepth;
        } else if (node.isArray()) {
            int maxChildDepth = 0;
            for (JsonNode element : node) {
                maxChildDepth = Math.max(maxChildDepth, calculateDepth(element));
            }
            return 1 + maxChildDepth;
        }
        return 1;
    }
}