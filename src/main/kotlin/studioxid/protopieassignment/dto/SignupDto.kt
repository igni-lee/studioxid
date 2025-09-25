package studioxid.protopieassignment.dto

import jakarta.validation.constraints.Pattern
import studioxid.protopieassignment.constant.UserRole
import studioxid.protopieassignment.constant.ValidEnum

data class SignupDto(
    @field:Pattern(
        regexp = "^[가-힣a-zA-Z\\s]{2,100}$",
        message = "이름은 2자 이상 100자 이하의 한글, 영문, 공백만 허용됩니다",
    )
    val name: String,
    @field:Pattern(
        regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
        message = "올바른 이메일 형식이 아닙니다",
    )
    val email: String,
    @field:Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?]).{8,16}$",
        message = "비밀번호는 최소 8자 이상 16자 이하, 대문자, 소문자, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다",
    )
    val password: String,
    @field:ValidEnum(message = "유효하지 않은 사용자 역할입니다")
    val role: UserRole = UserRole.MEMBER,
)
