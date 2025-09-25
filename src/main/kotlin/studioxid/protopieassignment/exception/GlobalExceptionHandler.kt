package studioxid.protopieassignment.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {
    /**
     * 사용자 관련 예외 처리
     */
    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFoundException(ex: UserNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(
                ErrorResponse(
                    timestamp = LocalDateTime.now(),
                    status = HttpStatus.NOT_FOUND.value(),
                    error = "User Not Found",
                    message = ex.message ?: "사용자를 찾을 수 없습니다",
                    path = "",
                ),
            )
    }

    @ExceptionHandler(EmailAlreadyExistsException::class)
    fun handleEmailAlreadyExistsException(ex: EmailAlreadyExistsException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(
                ErrorResponse(
                    timestamp = LocalDateTime.now(),
                    status = HttpStatus.CONFLICT.value(),
                    error = "Email Already Exists",
                    message = ex.message ?: "이미 존재하는 이메일입니다",
                    path = "",
                ),
            )
    }

    @ExceptionHandler(InvalidPasswordException::class)
    fun handleInvalidPasswordException(ex: InvalidPasswordException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(
                ErrorResponse(
                    timestamp = LocalDateTime.now(),
                    status = HttpStatus.UNAUTHORIZED.value(),
                    error = "Invalid Password",
                    message = ex.message ?: "비밀번호가 일치하지 않습니다",
                    path = "",
                ),
            )
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(ex: AccessDeniedException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(
                ErrorResponse(
                    timestamp = LocalDateTime.now(),
                    status = HttpStatus.FORBIDDEN.value(),
                    error = "Access Denied",
                    message = ex.message ?: "접근 권한이 없습니다",
                    path = "",
                ),
            )
    }

    @ExceptionHandler(PasswordPolicyViolationException::class)
    fun handlePasswordPolicyViolationException(ex: PasswordPolicyViolationException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    timestamp = LocalDateTime.now(),
                    status = HttpStatus.BAD_REQUEST.value(),
                    error = "Password Policy Violation",
                    message = ex.message ?: "비밀번호 정책을 위반했습니다",
                    path = "",
                ),
            )
    }

    /**
     * 유효성 검증 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ValidationErrorResponse> {
        val errors =
            ex.bindingResult.allErrors.map { error ->
                val fieldName = (error as FieldError).field
                val errorMessage = error.defaultMessage
                ValidationError(fieldName, errorMessage ?: "유효하지 않은 값입니다")
            }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                ValidationErrorResponse(
                    timestamp = LocalDateTime.now(),
                    status = HttpStatus.BAD_REQUEST.value(),
                    error = "Validation Failed",
                    message = "입력값 검증에 실패했습니다",
                    path = "",
                    validationErrors = errors,
                ),
            )
    }

    /**
     * 일반적인 예외 처리
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ErrorResponse(
                    timestamp = LocalDateTime.now(),
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    error = "Internal Server Error",
                    message = "서버 내부 오류가 발생했습니다",
                    path = "",
                ),
            )
    }
}

/**
 * 에러 응답 DTO
 */
data class ErrorResponse(
    val timestamp: LocalDateTime,
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
)

/**
 * 유효성 검증 에러 응답 DTO
 */
data class ValidationErrorResponse(
    val timestamp: LocalDateTime,
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val validationErrors: List<ValidationError>,
)

/**
 * 유효성 검증 에러 DTO
 */
data class ValidationError(
    val field: String,
    val message: String,
)
