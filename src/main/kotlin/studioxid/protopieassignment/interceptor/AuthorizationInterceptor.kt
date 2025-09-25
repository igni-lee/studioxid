package studioxid.protopieassignment.interceptor

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import studioxid.protopieassignment.exception.AccessDeniedException
import studioxid.protopieassignment.security.SecurityUtils
import java.util.regex.Pattern

/**
 * 사용자 권한 검증을 위한 Interceptor
 * URL 패턴을 기반으로 권한 검증을 수행합니다.
 * /users/{userId} 형태의 URL에 대해 현재 사용자가 해당 사용자에 대한 권한이 있는지 확인합니다.
 */
@Component
class AuthorizationInterceptor : HandlerInterceptor {
    companion object {
        // URL에서 userId를 추출하기 위한 정규식 패턴
        private val USER_ID_PATTERN = Pattern.compile("/users/(\\d+)")

        // 권한 검증이 필요한 HTTP 메서드들
        private val PROTECTED_METHODS = setOf("GET", "PUT", "DELETE")
    }

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        val requestURI = request.requestURI
        val httpMethod = request.method

        // 권한 검증이 필요한 메서드인지 확인
        if (!PROTECTED_METHODS.contains(httpMethod)) {
            return true
        }

        // /users/{userId} 패턴인지 확인
        if (!isUserSpecificEndpoint(requestURI)) {
            return true
        }

        // URL에서 userId 추출
        val userId =
            extractUserIdFromPath(requestURI)
                ?: throw AccessDeniedException("사용자 ID를 찾을 수 없습니다")

        // 권한 검증
        if (!SecurityUtils.hasPermissionForUser(userId)) {
            throw AccessDeniedException("해당 사용자에 대한 접근 권한이 없습니다")
        }

        return true
    }

    /**
     * URL이 사용자별 엔드포인트인지 확인합니다.
     * 예: /users/123 -> true, /users -> false, /users/signup -> false
     */
    private fun isUserSpecificEndpoint(path: String): Boolean {
        return USER_ID_PATTERN.matcher(path).matches()
    }

    /**
     * URL 경로에서 userId를 추출합니다.
     * 예: /users/123 -> 123
     */
    private fun extractUserIdFromPath(path: String): Long? {
        val matcher = USER_ID_PATTERN.matcher(path)
        return if (matcher.find()) {
            matcher.group(1).toLongOrNull()
        } else {
            null
        }
    }
}
