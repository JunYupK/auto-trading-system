package com.trading.explorer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(KrxApiProperties properties) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(properties.getApi().getTimeout())
                .followRedirect(true);

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(properties.getApi().getBaseUrl())
                .defaultHeader("User-Agent", "KRX-API-Explorer/1.0.0")
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)) // 16MB
                .build();
    }
}