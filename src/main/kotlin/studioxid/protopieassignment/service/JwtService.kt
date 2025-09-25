package studioxid.protopieassignment.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import studioxid.protopieassignment.constant.UserRole
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService {
    @Value("\${jwt.secret}")
    private lateinit var secretKey: String

    @Value("\${jwt.expiration}")
    private var expiration: Long = 86400000 // 24시간

    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(secretKey.toByteArray())
    }

    /**
     * JWT 토큰 생성
     */
    fun generateToken(
        userId: Long,
        email: String,
        role: UserRole,
    ): String {
        val now = Date()
        val expiryDate = Date(now.time + expiration)

        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("role", role.name)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact()
    }

    /**
     * JWT 토큰에서 사용자 ID 추출
     */
    fun getUserIdFromToken(token: String): Long {
        val claims = getClaimsFromToken(token)
        return claims.subject.toLong()
    }

    /**
     * JWT 토큰에서 이메일 추출
     */
    fun getEmailFromToken(token: String): String {
        val claims = getClaimsFromToken(token)
        return claims["email"] as String
    }

    /**
     * JWT 토큰에서 역할 추출
     */
    fun getRoleFromToken(token: String): UserRole {
        val claims = getClaimsFromToken(token)
        val roleString = claims["role"] as String
        return UserRole.valueOf(roleString)
    }

    /**
     * JWT 토큰 유효성 검증
     */
    fun validateToken(token: String): Boolean {
        return try {
            val claims = getClaimsFromToken(token)
            !isTokenExpired(claims)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * JWT 토큰에서 Claims 추출
     */
    fun getClaimsFromToken(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    /**
     * 토큰 만료 여부 확인
     */
    private fun isTokenExpired(claims: Claims): Boolean {
        return claims.expiration.before(Date())
    }

    /**
     * 토큰 만료 시간 반환 (초 단위)
     */
    fun getExpirationInSeconds(): Long {
        return expiration / 1000
    }
}
