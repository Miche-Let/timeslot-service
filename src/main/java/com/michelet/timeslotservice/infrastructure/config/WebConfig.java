package com.michelet.timeslotservice.infrastructure.config;

import com.michelet.common.auth.webmvc.interceptor.UserContextInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * 웹 계층 전역 설정을 담당하는 클래스입니다.
 * 공통 인증 모듈의 {@link UserContextInterceptor}를 등록하여
 * 모든 API 요청 시 사용자 컨텍스트를 스레드 로컬에 바인딩하는 역할을 수행합니다.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final UserContextInterceptor userContextInterceptor;

    /**
     * 애플리케이션의 인터셉터 체인을 구성합니다.
     * 인가(Authorization)가 필요한 경로와 공개(Public) 경로를 구분하여 등록합니다.
     *
     * @param registry 인터셉터 등록 레지스트리
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userContextInterceptor)
                .addPathPatterns("/api/v1/**", "/internal/v1/**")
                .excludePathPatterns("/api/v1/health-check");
    }
}