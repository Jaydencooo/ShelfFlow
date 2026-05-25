package com.shelfflow.services.gateway.web;

import com.shelfflow.services.common.config.RuntimeProperties;
import com.shelfflow.services.common.web.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Configuration
public class GatewayRequestFilters {
    private static final Logger ACCESS_LOGGER = LoggerFactory.getLogger("shelfflow.access");

    @Bean
    public GlobalFilter requestIdFilter() {
        return (exchange, chain) -> {
            String requestId = resolveRequestId(exchange);
            exchange.getResponse().getHeaders().set(RequestContext.REQUEST_ID_HEADER, requestId);
            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(builder -> builder.header(RequestContext.REQUEST_ID_HEADER, requestId))
                    .build();
            long startedAt = System.currentTimeMillis();
            return chain.filter(mutatedExchange)
                    .contextWrite(context -> context.put(RequestContext.REQUEST_ID_ATTRIBUTE, requestId))
                    .doFirst(() -> MDC.put("requestId", requestId))
                    .doFinally(signalType -> {
                        long durationMs = System.currentTimeMillis() - startedAt;
                        ACCESS_LOGGER.info(
                                "requestId={} method={} path={} status={} durationMs={} remoteAddr={} userAgent={}",
                                requestId,
                                exchange.getRequest().getMethod(),
                                exchange.getRequest().getURI().getPath(),
                                exchange.getResponse().getStatusCode(),
                                durationMs,
                                exchange.getRequest().getRemoteAddress(),
                                exchange.getRequest().getHeaders().getFirst(HttpHeaders.USER_AGENT)
                        );
                        MDC.remove("requestId");
                    });
        };
    }

    @Bean
    public CorsWebFilter corsWebFilter(RuntimeProperties runtimeProperties) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(runtimeProperties.getCorsAllowedOrigins());
        configuration.setAllowedMethods(List.of(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name()
        ));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return new CorsWebFilter(source);
    }

    private String resolveRequestId(ServerWebExchange exchange) {
        String requestId = exchange.getRequest().getHeaders().getFirst(RequestContext.REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return requestId;
    }
}
