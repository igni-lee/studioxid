package studioxid.protopieassignment.service

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import studioxid.protopieassignment.dto.PageDto
import studioxid.protopieassignment.dto.SigninDto
import studioxid.protopieassignment.dto.SignupDto
import studioxid.protopieassignment.dto.TokenDto
import studioxid.protopieassignment.dto.UserDto
import studioxid.protopieassignment.dto.UserUpdateDto
import studioxid.protopieassignment.entity.UserEntity
import studioxid.protopieassignment.exception.AccessDeniedException
import studioxid.protopieassignment.exception.EmailAlreadyExistsException
import studioxid.protopieassignment.exception.InvalidPasswordException
import studioxid.protopieassignment.exception.UserNotFoundException
import studioxid.protopieassignment.repository.UserCacheRepository
import studioxid.protopieassignment.repository.UserRepository
import studioxid.protopieassignment.repository.UserRepositorySupport
import studioxid.protopieassignment.security.SecurityUtils

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userRepositorySupport: UserRepositorySupport,
    private val userCacheRepository: UserCacheRepository,
    private val passwordService: PasswordService,
    private val jwtService: JwtService,
    private val asyncUserDeletionService: AsyncUserDeletionService,
) {
    /**
     * 사용자 회원가입
     */
    @Transactional
    fun signup(signupDto: SignupDto) {
        // 이메일 중복 체크
        if (userRepositorySupport.existsByEmailAndNotDeleted(signupDto.email)) {
            throw EmailAlreadyExistsException()
        }

        // 비밀번호 해시화
        val passwordHash = passwordService.createPasswordHash(signupDto.password)

        // 사용자 엔티티 생성
        val userEntity =
            UserEntity(
                name = signupDto.name,
                email = signupDto.email,
                password = passwordHash.encodedPassword,
                salt = passwordHash.salt,
                role = signupDto.role,
            )

        // 사용자 저장
        userRepository.save(userEntity)
    }

    /**
     * 사용자 로그인
     */
    @Transactional(readOnly = true)
    fun signin(signinDto: SigninDto): TokenDto {
        // 사용자 조회
        val user =
            userRepositorySupport.findByEmailAndNotDeleted(signinDto.email)
                ?: throw UserNotFoundException("이메일 또는 비밀번호가 올바르지 않습니다")

        // 비밀번호 검증
        if (!passwordService.matches(signinDto.password, user.salt, user.password)) {
            throw InvalidPasswordException("이메일 또는 비밀번호가 올바르지 않습니다")
        }

        // JWT 토큰 생성
        val token = jwtService.generateToken(user.id!!, user.email, user.role)

        return TokenDto(
            accessToken = token,
            expiresIn = jwtService.getExpirationInSeconds(),
        )
    }

    /**
     * 사용자 정보 조회
     */
    @Transactional(readOnly = true)
    fun getUser(userId: Long): UserDto {
        // 캐시에서 먼저 조회
        val cachedUser = userCacheRepository.findUserById(userId)
        if (cachedUser != null) {
            return cachedUser
        }

        // DB에서 조회
        val user =
            userRepositorySupport.findByIdAndNotDeleted(userId)
                ?: throw UserNotFoundException()

        val userDto = user.toUserDto()

        // 캐시에 저장
        userCacheRepository.saveUser(userDto)

        return userDto
    }

    /**
     * 사용자 정보 수정
     */
    @Transactional
    fun updateUser(
        userId: Long,
        updateDto: UserUpdateDto,
    ): UserDto {
        // 사용자 조회
        val user =
            userRepositorySupport.findByIdAndNotDeleted(userId)
                ?: throw UserNotFoundException()

        // 이메일 변경 시 중복 체크
        if (updateDto.email?.isNotBlank() == true && updateDto.email != user.email) {
            if (userRepositorySupport.existsByEmailAndNotDeleted(updateDto.email)) {
                throw EmailAlreadyExistsException()
            }
        }

        // 정보 업데이트
        user.name = updateDto.name ?: user.name
        user.email = updateDto.email ?: user.email

        // 저장
        userRepositorySupport.update(user)

        // 캐시 무효화
        userCacheRepository.deleteUser(userId)
        userCacheRepository.deleteUserList()

        return user.toUserDto()
    }

    /**
     * 사용자 삭제 (소프트 삭제)
     */
    @Transactional
    fun deleteUser(userId: Long) {
        // 사용자 존재 확인
        val user =
            userRepositorySupport.findByIdAndNotDeleted(userId)
                ?: throw UserNotFoundException()

        // 소프트 삭제
        userRepositorySupport.softDeleteById(userId)

        // 캐시 무효화
        userCacheRepository.deleteUser(userId)
        userCacheRepository.deleteUserList()

        // 비동기 처리로 파일 정리, 감사 로그 등 수행
        asyncUserDeletionService.sendUserDeletionMessage(user)
    }

    /**
     * 모든 사용자 조회 (Admin 전용)
     */
    @Transactional(readOnly = true)
    fun getUsers(
        page: Int = 0,
        size: Int = 10,
        sort: String = "createdAt",
        direction: String = "desc",
    ): PageDto<UserDto> {
        // Admin 권한 검증
        if (!SecurityUtils.isAdmin()) {
            throw AccessDeniedException()
        }

        // 페이징 및 정렬 설정
        val sortDirection = if (direction.equals("asc", ignoreCase = true)) Sort.Direction.ASC else Sort.Direction.DESC
        val pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort))

        val userPage = userRepositorySupport.findAllNotDeleted(pageable)
        val users = userPage.content.map { it.toUserDto() }

        return PageDto(
            content = users,
            page = userPage.number,
            size = userPage.size,
            totalElements = userPage.totalElements,
            totalPages = userPage.totalPages,
            first = userPage.isFirst,
            last = userPage.isLast,
        )
    }
}
