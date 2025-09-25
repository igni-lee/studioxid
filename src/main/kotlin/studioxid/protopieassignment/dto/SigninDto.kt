package studioxid.protopieassignment.dto

import jakarta.validation.constraints.Pattern

data class SigninDto(
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
)
