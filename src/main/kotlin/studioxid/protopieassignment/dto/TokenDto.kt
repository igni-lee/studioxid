package studioxid.protopieassignment.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "JWT 토큰 응답")
data class TokenDto(
    @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    val accessToken: String,
    @Schema(description = "토큰 만료 시간 (초)", example = "86400")
    val expiresIn: Long,
)
