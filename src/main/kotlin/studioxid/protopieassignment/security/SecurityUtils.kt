package studioxid.protopieassignment.security

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import studioxid.protopieassignment.constant.UserRole

object SecurityUtils {
    /**
     * 현재 인증된 사용자 ID 반환
     */
    fun getCurrentUserId(): Long? {
        val authentication = getCurrentAuthentication()
        return authentication?.principal?.toString()?.toLongOrNull()
    }

    /**
     * 현재 인증된 사용자의 역할 반환
     */
    fun getCurrentUserRole(): UserRole? {
        val authentication = getCurrentAuthentication()
        return authentication?.authorities?.firstOrNull()?.authority
            ?.removePrefix("ROLE_")
            ?.let { UserRole.valueOf(it) }
    }

    /**
     * 현재 사용자가 Admin인지 확인
     */
    fun isAdmin(): Boolean {
        return getCurrentUserRole() == UserRole.ADMIN
    }

    /**
     * 현재 사용자가 특정 사용자 ID와 일치하는지 확인
     */
    fun isCurrentUser(userId: Long): Boolean {
        return getCurrentUserId() == userId
    }

    /**
     * 현재 사용자가 특정 사용자에 대한 권한이 있는지 확인
     * (본인이거나 Admin인 경우)
     */
    fun hasPermissionForUser(userId: Long): Boolean {
        return isAdmin() || isCurrentUser(userId)
    }

    /**
     * 현재 인증 정보 반환
     */
    private fun getCurrentAuthentication(): Authentication? {
        return SecurityContextHolder.getContext().authentication
    }
}
