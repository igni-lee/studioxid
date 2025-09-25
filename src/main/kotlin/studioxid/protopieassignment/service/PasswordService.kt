package studioxid.protopieassignment.service

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.util.Base64

@Service
class PasswordService {
    private val passwordEncoder = BCryptPasswordEncoder(12) // 강도 12 (권장)
    private val secureRandom = SecureRandom()

    companion object {
        private const val SALT_LENGTH = 32 // 32바이트 Salt
    }

    /**
     * 비밀번호와 Salt를 조합하여 해시화
     */
    fun encodePassword(
        password: String,
        salt: String,
    ): String {
        val saltedPassword = password + salt
        return passwordEncoder.encode(saltedPassword)
    }

    /**
     * 비밀번호 검증
     */
    fun matches(
        rawPassword: String,
        salt: String,
        encodedPassword: String,
    ): Boolean {
        val saltedPassword = rawPassword + salt
        return passwordEncoder.matches(saltedPassword, encodedPassword)
    }

    /**
     * 랜덤 Salt 생성
     */
    fun generateSalt(): String {
        val saltBytes = ByteArray(SALT_LENGTH)
        secureRandom.nextBytes(saltBytes)
        return Base64.getEncoder().encodeToString(saltBytes)
    }

    /**
     * 비밀번호 해시 정보 생성 (비밀번호 + Salt)
     */
    fun createPasswordHash(password: String): PasswordHash {
        val salt = generateSalt()
        val encodedPassword = encodePassword(password, salt)

        return PasswordHash(
            encodedPassword = encodedPassword,
            salt = salt,
        )
    }
}

/**
 * 비밀번호 해시 정보
 */
data class PasswordHash(
    val encodedPassword: String,
    val salt: String,
)
