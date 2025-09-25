package studioxid.protopieassignment.repository

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import studioxid.protopieassignment.dto.UserDto
import java.time.Duration

@Repository
class UserCacheRepository(
    private val redisTemplate: RedisTemplate<String, Any>,
) {
    companion object {
        private const val USER_CACHE_PREFIX = "user:"
        private const val USER_LIST_CACHE_PREFIX = "users:"
        private const val CACHE_TTL_HOURS = 1L
    }

    /**
     * 사용자 정보 캐시 저장
     */
    fun saveUser(user: UserDto) {
        val key = "$USER_CACHE_PREFIX${user.id}"
        redisTemplate.opsForValue().set(key, user, Duration.ofHours(CACHE_TTL_HOURS))
    }

    /**
     * 사용자 정보 캐시 조회
     */
    fun findUserById(id: Long): UserDto? {
        val key = "$USER_CACHE_PREFIX$id"
        return redisTemplate.opsForValue().get(key) as? UserDto
    }

    /**
     * 사용자 캐시 삭제
     */
    fun deleteUser(id: Long) {
        val key = "$USER_CACHE_PREFIX$id"
        redisTemplate.delete(key)
    }

    /**
     * 사용자 목록 캐시 삭제 (모든 페이지)
     */
    fun deleteUserList() {
        val pattern = "$USER_LIST_CACHE_PREFIX*"
        val keys = redisTemplate.keys(pattern)
        if (keys.isNotEmpty()) {
            redisTemplate.delete(keys)
        }
    }
}
