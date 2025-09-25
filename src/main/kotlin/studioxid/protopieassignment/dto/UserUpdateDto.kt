package studioxid.protopieassignment.dto

import jakarta.validation.constraints.Pattern

data class UserUpdateDto(
    @field:Pattern(
        regexp = "^[가-힣a-zA-Z\\s]{2,100}$",
        message = "이름은 2자 이상 100자 이하의 한글, 영문, 공백만 허용됩니다",
    )
    val name: String?,
    @field:Pattern(
        regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
        message = "올바른 이메일 형식이 아닙니다",
    )
    val email: String?,
)
