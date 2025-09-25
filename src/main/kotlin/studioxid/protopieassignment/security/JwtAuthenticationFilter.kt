package studioxid.protopieassignment.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import studioxid.protopieassignment.service.JwtService

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
) : OncePerRequestFilter() {
    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val authHeader = request.getHeader(AUTHORIZATION_HEADER)

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            val token = authHeader.substring(BEARER_PREFIX.length)

            if (jwtService.validateToken(token)) {
                val userId = jwtService.getUserIdFromToken(token)
                val email = jwtService.getEmailFromToken(token)
                val role = jwtService.getRoleFromToken(token)

                val authorities = listOf(SimpleGrantedAuthority("ROLE_${role.name}"))

                val authentication =
                    UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        authorities,
                    ).apply {
                        details = WebAuthenticationDetailsSource().buildDetails(request)
                    }

                SecurityContextHolder.getContext().authentication = authentication
            }
        } catch (e: Exception) {
            logger.error("JWT 토큰 처리 중 오류 발생", e)
        }

        filterChain.doFilter(request, response)
    }
}
