package studioxid.protopieassignment.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import studioxid.protopieassignment.dto.PageDto
import studioxid.protopieassignment.dto.SigninDto
import studioxid.protopieassignment.dto.SignupDto
import studioxid.protopieassignment.dto.TokenDto
import studioxid.protopieassignment.dto.UserDto
import studioxid.protopieassignment.dto.UserUpdateDto
import studioxid.protopieassignment.service.UserService

@Tag(name = "User Management", description = "사용자 관리 API")
@Validated
@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
) {
    @Operation(
        summary = "사용자 회원가입",
        description = "새로운 사용자를 등록합니다. 이메일 중복 체크와 비밀번호 정책 검증을 수행합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "회원가입 성공",
                content = [Content(schema = Schema(implementation = TokenDto::class))],
            ),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (이메일 중복, 비밀번호 정책 위반 등)"),
            ApiResponse(responseCode = "422", description = "입력값 검증 실패"),
        ],
    )
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    fun signup(
        @Valid @RequestBody signupDto: SignupDto,
    ) = userService.signup(signupDto)

    @Operation(
        summary = "사용자 로그인",
        description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "로그인 성공",
                content = [Content(schema = Schema(implementation = TokenDto::class))],
            ),
            ApiResponse(responseCode = "401", description = "인증 실패 (잘못된 이메일 또는 비밀번호)"),
            ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
        ],
    )
    @PostMapping("/signin")
    @ResponseStatus(HttpStatus.OK)
    fun signin(
        @Valid @RequestBody signinDto: SigninDto,
    ) = userService.signin(signinDto)

    @Operation(
        summary = "사용자 삭제(탈퇴)",
        description = "사용자를 삭제합니다 (소프트 삭제). 본인 또는 Admin만 삭제 가능합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "삭제 성공"),
            ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUser(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable userId: Long,
    ) = userService.deleteUser(userId)

    @Operation(
        summary = "특정 사용자 정보 수정",
        description = "특정 사용자 정보를 수정합니다. 본인 또는 Admin만 수정 가능합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "수정 성공", content = [Content(schema = Schema(implementation = UserDto::class))]),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (이메일 중복 등)"),
            ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    fun updateUser(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable userId: Long,
        @Valid @RequestBody updateDto: UserUpdateDto,
    ) = userService.updateUser(userId, updateDto)

    @Operation(
        summary = "특정 사용자 정보 조회",
        description = "특정 사용자의 정보를 조회합니다. 본인 또는 Admin만 조회 가능합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공", content = [Content(schema = Schema(implementation = UserDto::class))]),
            ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    fun getUser(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable userId: Long,
    ) = userService.getUser(userId)

    @Operation(
        summary = "전체 사용자 조회",
        description = "모든 사용자 목록을 조회합니다. Admin만 접근 가능하며 페이징을 지원합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공", content = [Content(schema = Schema(implementation = PageDto::class))]),
            ApiResponse(responseCode = "403", description = "Admin 권한 필요"),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getUsers(
        @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기", example = "10")
        @RequestParam(defaultValue = "10") size: Int,
        @Parameter(description = "정렬 기준", example = "createdAt")
        @RequestParam(defaultValue = "createdAt") sort: String,
        @Parameter(description = "정렬 방향", example = "desc")
        @RequestParam(defaultValue = "desc") direction: String,
    ) = userService.getUsers(page, size, sort, direction)
}
