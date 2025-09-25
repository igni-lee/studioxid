package studioxid.protopieassignment.dto

import studioxid.protopieassignment.constant.UserRole
import java.time.LocalDateTime

data class UserDto(
    val id: Long,
    val name: String,
    val email: String,
    val role: UserRole,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
