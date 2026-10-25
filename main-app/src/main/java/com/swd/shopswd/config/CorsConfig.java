package com.swd.shopswd.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 프론트엔드(shopswd-mobile) 에서 직접 호출하는 경우를 위한 CORS 설정.
 * 쿠키/세션을 사용하지 않으므로 credentials 는 허용하지 않는다.
 */
@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer(@Value("${app.cors.allowed-origin-patterns}") String[] allowedOriginPatterns) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOriginPatterns(allowedOriginPatterns)
                    .allowCredentials(false)
                    .allowedHeaders("*")
                    .allowedMethods("GET", "POST", "PATCH", "DELETE", "OPTIONS");
            }
        };
    }
}
