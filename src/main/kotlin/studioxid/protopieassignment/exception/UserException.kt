package studioxid.protopieassignment.exception

/**
 * 사용자 관련 예외의 기본 클래스
 */
abstract class UserException(message: String) : RuntimeException(message)

/**
 * 사용자를 찾을 수 없을 때 발생하는 예외
 */
class UserNotFoundException(message: String = "사용자를 찾을 수 없습니다") : UserException(message)

/**
 * 이메일이 이미 존재할 때 발생하는 예외
 */
class EmailAlreadyExistsException(message: String = "이미 존재하는 이메일입니다") : UserException(message)

/**
 * 비밀번호가 일치하지 않을 때 발생하는 예외
 */
class InvalidPasswordException(message: String = "비밀번호가 일치하지 않습니다") : UserException(message)

/**
 * 권한이 없을 때 발생하는 예외
 */
class AccessDeniedException(message: String = "접근 권한이 없습니다") : UserException(message)

/**
 * 비밀번호 정책 위반 시 발생하는 예외
 */
class PasswordPolicyViolationException(message: String) : UserException(message)
