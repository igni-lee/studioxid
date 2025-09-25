package studioxid.protopieassignment.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import studioxid.protopieassignment.interceptor.AuthorizationInterceptor

/**
 * Web MVC 설정
 * Interceptor를 등록하여 권한 검증을 수행합니다.
 */
@Configuration
class WebMvcConfiguration(
    private val authorizationInterceptor: AuthorizationInterceptor,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authorizationInterceptor)
            .addPathPatterns("/users/**")
            .excludePathPatterns(
                "/users/signup",
                "/users/signin",
                "/users",
            )
    }
}
