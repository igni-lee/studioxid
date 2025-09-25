package studioxid.protopieassignment.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import studioxid.protopieassignment.TestContainersConfiguration
import studioxid.protopieassignment.constant.UserRole
import studioxid.protopieassignment.dto.SigninDto
import studioxid.protopieassignment.dto.SignupDto
import studioxid.protopieassignment.dto.UserUpdateDto
import studioxid.protopieassignment.entity.UserEntity
import studioxid.protopieassignment.repository.UserRepository
import studioxid.protopieassignment.service.PasswordService

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("UserController 통합 테스트")
class UserControllerTest : TestContainersConfiguration() {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var passwordService: PasswordService

    // Helper function to get JWT token
    private fun getToken(
        email: String,
        password: String,
    ): String {
        val signinDto = SigninDto(email = email, password = password)

        val tokenResponse =
            mockMvc.perform(
                post("/users/signin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signinDto)),
            )
                .andExpect(status().isOk)
                .andReturn()

        return objectMapper.readTree(tokenResponse.response.contentAsString)["accessToken"].asText()
    }

    // Helper function to create test user directly in database
    private fun createTestUser(
        name: String,
        email: String,
        password: String,
        role: UserRole,
    ): UserEntity {
        val passwordHash = passwordService.createPasswordHash(password)
        val userEntity =
            UserEntity(
                name = name,
                email = email,
                password = passwordHash.encodedPassword,
                salt = passwordHash.salt,
                role = role,
            )
        return userRepository.save(userEntity)
    }

    @Nested
    @DisplayName("회원가입 API 테스트")
    inner class SignupApiTest {
        @Test
        @DisplayName("정상적인 회원가입이 성공한다")
        fun `회원가입 성공 테스트`() {
            val signupDto =
                SignupDto(
                    name = "테스트사용자",
                    email = "test@example.com",
                    password = "TestPass123!",
                    role = UserRole.MEMBER,
                )

            mockMvc.perform(
                post("/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupDto)),
            )
                .andExpect(status().isCreated)
        }

        @Test
        @DisplayName("Admin 역할로 회원가입이 성공한다")
        fun `Admin 회원가입 성공 테스트`() {
            val signupDto =
                SignupDto(
                    name = "Admin사용자",
                    email = "admin@example.com",
                    password = "AdminPass123!",
                    role = UserRole.ADMIN,
                )

            mockMvc.perform(
                post("/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupDto)),
            )
                .andExpect(status().isCreated)
        }

        @Test
        @DisplayName("이메일 중복 시 회원가입이 실패한다")
        fun `이메일 중복 회원가입 실패 테스트`() {
            val signupDto1 =
                SignupDto(
                    name = "첫번째사용자",
                    email = "duplicate@example.com",
                    password = "TestPass123!",
                    role = UserRole.MEMBER,
                )

            val signupDto2 =
                SignupDto(
                    name = "두번째사용자",
                    email = "duplicate@example.com",
                    password = "TestPass123!",
                    role = UserRole.MEMBER,
                )

            // 첫 번째 회원가입 성공
            mockMvc.perform(
                post("/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupDto1)),
            ).andExpect(status().isCreated)

            // 두 번째 회원가입 실패 (이메일 중복)
            mockMvc.perform(
                post("/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupDto2)),
            )
                .andExpect(status().isConflict)
                .andExpect(jsonPath("$.error").value("Email Already Exists"))
        }

        @Test
        @DisplayName("잘못된 이메일 형식으로 회원가입이 실패한다")
        fun `잘못된 이메일 형식 회원가입 실패 테스트`() {
            val signupDto =
                SignupDto(
                    name = "테스트사용자",
                    email = "invalid-email",
                    password = "TestPass123!",
                    role = UserRole.MEMBER,
                )

            mockMvc.perform(
                post("/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupDto)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.error").value("Validation Failed"))
        }

        @Test
        @DisplayName("빈 이메일로 회원가입이 실패한다")
        fun `빈 이메일 회원가입 실패 테스트`() {
            val signupDto =
                SignupDto(
                    name = "테스트사용자",
                    email = "",
                    password = "TestPass123!",
                    role = UserRole.MEMBER,
                )

            mockMvc.perform(
                post("/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupDto)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.error").value("Validation Failed"))
        }

        @Test
        @DisplayName("비밀번호 정책 위반 시 회원가입이 실패한다")
        fun `비밀번호 정책 위반 회원가입 실패 테스트`() {
            val signupDto =
                SignupDto(
                    name = "테스트사용자",
                    email = "test@example.com",
                    password = "weak",
                    role = UserRole.MEMBER,
                )

            mockMvc.perform(
                post("/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupDto)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.error").value("Validation Failed"))
        }

        @Test
        @DisplayName("대문자 없는 비밀번호로 회원가입이 실패한다")
        fun `대문자 없는 비밀번호 회원가입 실패 테스트`() {
            val signupDto =
                SignupDto(
                    name = "테스트사용자",
                    email = "test@example.com",
                    password = "testpass123!",
                    role = UserRole.MEMBER,
                )

            mockMvc.perform(
                post("/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupDto)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.error").value("Validation Failed"))
        }

        @Test
        @DisplayName("소문자 없는 비밀번호로 회원가입이 실패한다")
        fun `소문자 없는 비밀번호 회원가입 실패 테스트`() {
            val signupDto =
                SignupDto(
                    name = "테스트사용자",
                    email = "test@example.com",
                    password = "TESTPASS123!",
                    role = UserRole.MEMBER,
                )

            mockMvc.perform(
                post("/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupDto)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.error").value("Validation Failed"))
        }

        @Test
        @DisplayName("숫자 없는 비밀번호로 회원가입이 실패한다")
        fun `숫자 없는 비밀번호 회원가입 실패 테스트`() {
            val signupDto =
                SignupDto(
                    name = "테스트사용자",
                    email = "test@example.com",
                    password = "TestPass!",
                    role = UserRole.MEMBER,
                )

            mockMvc.perform(
                post("/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupDto)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.error").value("Validation Failed"))
        }

        @Test
        @DisplayName("특수문자 없는 비밀번호로 회원가입이 실패한다")
        fun `특수문자 없는 비밀번호 회원가입 실패 테스트`() {
            val signupDto =
                SignupDto(
                    name = "테스트사용자",
                    email = "test@example.com",
                    password = "TestPass123",
                    role = UserRole.MEMBER,
                )

            mockMvc.perform(
                post("/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupDto)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.error").value("Validation Failed"))
        }

        @Test
        @DisplayName("비밀번호가 너무 짧을 때 회원가입이 실패한다")
        fun `비밀번호 길이 부족 회원가입 실패 테스트`() {
            val signupDto =
                SignupDto(
                    name = "테스트사용자",
                    email = "test@example.com",
                    password = "Test1!",
                    role = UserRole.MEMBER,
                )

            mockMvc.perform(
                post("/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupDto)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.error").value("Validation Failed"))
        }

        @Test
        @DisplayName("비밀번호가 너무 길 때 회원가입이 실패한다")
        fun `비밀번호 길이 초과 회원가입 실패 테스트`() {
            val signupDto =
                SignupDto(
                    name = "테스트사용자",
                    email = "test@example.com",
                    password = "TestPass123!VeryLongPassword",
                    role = UserRole.MEMBER,
                )

            mockMvc.perform(
                post("/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupDto)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.error").value("Validation Failed"))
        }

        @Test
        @DisplayName("이름이 너무 짧을 때 회원가입이 실패한다")
        fun `이름 길이 부족 회원가입 실패 테스트`() {
            val signupDto =
                SignupDto(
                    name = "A",
                    email = "test@example.com",
                    password = "TestPass123!",
                    role = UserRole.MEMBER,
                )

            mockMvc.perform(
                post("/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupDto)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.error").value("Validation Failed"))
        }

        @Test
        @DisplayName("이름이 너무 길 때 회원가입이 실패한다")
        fun `이름 길이 초과 회원가입 실패 테스트`() {
            val longName = "A".repeat(101)
            val signupDto =
                SignupDto(
                    name = longName,
                    email = "test@example.com",
                    password = "TestPass123!",
                    role = UserRole.MEMBER,
                )

            mockMvc.perform(
                post("/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupDto)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.error").value("Validation Failed"))
        }

        @Test
        @DisplayName("이름에 허용되지 않는 문자가 포함될 때 회원가입이 실패한다")
        fun `이름에 특수문자 포함 회원가입 실패 테스트`() {
            val signupDto =
                SignupDto(
                    name = "테스트사용자123",
                    email = "test@example.com",
                    password = "TestPass123!",
                    role = UserRole.MEMBER,
                )

            mockMvc.perform(
                post("/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupDto)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.error").value("Validation Failed"))
        }

        @Test
        @DisplayName("빈 이름으로 회원가입이 실패한다")
        fun `빈 이름 회원가입 실패 테스트`() {
            val signupDto =
                SignupDto(
                    name = "",
                    email = "test@example.com",
                    password = "TestPass123!",
                    role = UserRole.MEMBER,
                )

            mockMvc.perform(
                post("/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupDto)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.error").value("Validation Failed"))
        }

        @Test
        @DisplayName("빈 비밀번호로 회원가입이 실패한다")
        fun `빈 비밀번호 회원가입 실패 테스트`() {
            val signupDto =
                SignupDto(
                    name = "테스트사용자",
                    email = "test@example.com",
                    password = "",
                    role = UserRole.MEMBER,
                )

            mockMvc.perform(
                post("/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupDto)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.error").value("Validation Failed"))
        }
    }

    @Nested
    @DisplayName("로그인 API 테스트")
    inner class SigninApiTest {
        @Test
        @DisplayName("정상적인 로그인이 성공한다")
        fun `로그인 성공 테스트`() {
            // 미리 사용자 데이터 생성
            createTestUser(
                name = "로그인테스트",
                email = "login@example.com",
                password = "LoginPass123!",
                role = UserRole.MEMBER,
            )

            // 로그인 테스트
            val signinDto =
                SigninDto(
                    email = "login@example.com",
                    password = "LoginPass123!",
                )

            mockMvc.perform(
                post("/users/signin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signinDto)),
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.expiresIn").exists())
        }

        @Test
        @DisplayName("Admin 사용자 로그인이 성공한다")
        fun `Admin 로그인 성공 테스트`() {
            // 미리 Admin 사용자 데이터 생성
            createTestUser(
                name = "Admin로그인테스트",
                email = "adminlogin@example.com",
                password = "AdminPass123!",
                role = UserRole.ADMIN,
            )

            // Admin 로그인 테스트
            val signinDto =
                SigninDto(
                    email = "adminlogin@example.com",
                    password = "AdminPass123!",
                )

            mockMvc.perform(
                post("/users/signin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signinDto)),
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.expiresIn").exists())
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인이 실패한다")
        fun `잘못된 비밀번호 로그인 실패 테스트`() {
            // 미리 사용자 데이터 생성
            createTestUser(
                name = "로그인테스트",
                email = "wrongpass@example.com",
                password = "LoginPass123!",
                role = UserRole.MEMBER,
            )

            // 잘못된 비밀번호로 로그인 시도
            val signinDto =
                SigninDto(
                    email = "wrongpass@example.com",
                    password = "WrongPass123!",
                )

            mockMvc.perform(
                post("/users/signin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signinDto)),
            )
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.error").value("Invalid Password"))
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인이 실패한다")
        fun `존재하지 않는 이메일 로그인 실패 테스트`() {
            val signinDto =
                SigninDto(
                    email = "nonexistent@example.com",
                    password = "TestPass123!",
                )

            mockMvc.perform(
                post("/users/signin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signinDto)),
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.error").value("User Not Found"))
        }

        @Test
        @DisplayName("잘못된 이메일 형식으로 로그인이 실패한다")
        fun `잘못된 이메일 형식 로그인 실패 테스트`() {
            val signinDto =
                SigninDto(
                    email = "invalid-email",
                    password = "TestPass123!",
                )

            mockMvc.perform(
                post("/users/signin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signinDto)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.error").value("Validation Failed"))
        }

        @Test
        @DisplayName("빈 이메일로 로그인이 실패한다")
        fun `빈 이메일 로그인 실패 테스트`() {
            val signinDto =
                SigninDto(
                    email = "",
                    password = "TestPass123!",
                )

            mockMvc.perform(
                post("/users/signin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signinDto)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.error").value("Validation Failed"))
        }

        @Test
        @DisplayName("빈 비밀번호로 로그인이 실패한다")
        fun `빈 비밀번호 로그인 실패 테스트`() {
            val signinDto =
                SigninDto(
                    email = "test@example.com",
                    password = "",
                )

            mockMvc.perform(
                post("/users/signin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signinDto)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.error").value("Validation Failed"))
        }

        @Test
        @DisplayName("비밀번호 정책 위반으로 로그인이 실패한다")
        fun `비밀번호 정책 위반 로그인 실패 테스트`() {
            val signinDto =
                SigninDto(
                    email = "test@example.com",
                    password = "weak",
                )

            mockMvc.perform(
                post("/users/signin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signinDto)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.error").value("Validation Failed"))
        }
    }

    @Nested
    @DisplayName("사용자 정보 조회 API 테스트")
    inner class GetUserApiTest {
        @Test
        @DisplayName("JWT 토큰으로 본인 정보 조회가 성공한다")
        fun `JWT 토큰으로 본인 정보 조회 성공`() {
            // 미리 사용자 데이터 생성
            val user =
                createTestUser(
                    name = "본인조회테스트",
                    email = "self@example.com",
                    password = "SelfPass123!",
                    role = UserRole.MEMBER,
                )

            // 로그인하여 토큰 획득
            val token = getToken("self@example.com", "SelfPass123!")

            // 토큰으로 본인 정보 조회
            mockMvc.perform(
                get("/users/${user.id}")
                    .header("Authorization", "Bearer $token"),
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(user.id))
                .andExpect(jsonPath("$.email").value("self@example.com"))
                .andExpect(jsonPath("$.name").value("본인조회테스트"))
                .andExpect(jsonPath("$.role").value("MEMBER"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())
        }

        @Test
        @DisplayName("Admin 권한으로 다른 사용자 정보 조회가 성공한다")
        fun `Admin 권한으로 다른 사용자 정보 조회 성공`() {
            // 미리 일반 사용자 데이터 생성
            val memberUser =
                createTestUser(
                    name = "일반사용자",
                    email = "member@example.com",
                    password = "MemberPass123!",
                    role = UserRole.MEMBER,
                )

            // 미리 Admin 사용자 데이터 생성
            createTestUser(
                name = "Admin사용자",
                email = "admin@example.com",
                password = "AdminPass123!",
                role = UserRole.ADMIN,
            )

            // Admin으로 로그인하여 토큰 획득
            val adminToken = getToken("admin@example.com", "AdminPass123!")

            // Admin이 일반 사용자 정보 조회
            mockMvc.perform(
                get("/users/${memberUser.id}")
                    .header("Authorization", "Bearer $adminToken"),
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(memberUser.id))
                .andExpect(jsonPath("$.email").value("member@example.com"))
                .andExpect(jsonPath("$.name").value("일반사용자"))
                .andExpect(jsonPath("$.role").value("MEMBER"))
        }

        @Test
        @DisplayName("일반 사용자가 다른 사용자 정보 조회 시 권한 없음")
        fun `일반 사용자가 다른 사용자 정보 조회 시 권한 없음`() {
            // 미리 첫 번째 사용자 데이터 생성
            createTestUser(
                name = "사용자1",
                email = "user1@example.com",
                password = "User1Pass123!",
                role = UserRole.MEMBER,
            )

            // 미리 두 번째 사용자 데이터 생성
            val user2 =
                createTestUser(
                    name = "사용자2",
                    email = "user2@example.com",
                    password = "User2Pass123!",
                    role = UserRole.MEMBER,
                )

            // 사용자1로 로그인하여 토큰 획득
            val user1Token = getToken("user1@example.com", "User1Pass123!")

            // 사용자1이 사용자2 정보 조회 시도 (권한 없음)
            mockMvc.perform(
                get("/users/${user2.id}")
                    .header("Authorization", "Bearer $user1Token"),
            )
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.error").value("Access Denied"))
        }

        @Test
        @DisplayName("토큰 없이 사용자 정보 조회 시 인증 실패")
        fun `토큰 없이 사용자 정보 조회 시 인증 실패`() {
            mockMvc.perform(get("/users/1"))
                .andExpect(status().isForbidden)
        }

        @Test
        @DisplayName("잘못된 토큰으로 사용자 정보 조회 시 인증 실패")
        fun `잘못된 토큰으로 사용자 정보 조회 시 인증 실패`() {
            mockMvc.perform(
                get("/users/1")
                    .header("Authorization", "Bearer invalid.token.here"),
            )
                .andExpect(status().isForbidden)
        }

        @Test
        @DisplayName("존재하지 않는 사용자 정보 조회 시 실패")
        fun `존재하지 않는 사용자 정보 조회 실패`() {
            // 미리 Admin 사용자 데이터 생성
            createTestUser(
                name = "Admin사용자",
                email = "adminnonexist@example.com",
                password = "AdminPass123!",
                role = UserRole.ADMIN,
            )

            // Admin으로 로그인하여 토큰 획득
            val adminToken = getToken("adminnonexist@example.com", "AdminPass123!")

            // 존재하지 않는 사용자 조회 시도
            mockMvc.perform(
                get("/users/999")
                    .header("Authorization", "Bearer $adminToken"),
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.error").value("User Not Found"))
        }
    }

    @Nested
    @DisplayName("사용자 정보 수정 API 테스트")
    inner class UpdateUserApiTest {
        @Test
        @DisplayName("본인 정보 수정이 성공한다")
        fun `본인 정보 수정 성공`() {
            // 미리 사용자 데이터 생성
            val user =
                createTestUser(
                    name = "수정테스트사용자",
                    email = "update@example.com",
                    password = "UpdatePass123!",
                    role = UserRole.MEMBER,
                )

            // 로그인하여 토큰 획득
            val token = getToken("update@example.com", "UpdatePass123!")

            // 본인 정보 수정
            val updateDto =
                UserUpdateDto(
                    name = "수정된이름",
                    email = "updated@example.com",
                )

            mockMvc.perform(
                put("/users/${user.id}")
                    .header("Authorization", "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDto)),
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.name").value("수정된이름"))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
        }

        @Test
        @DisplayName("Admin이 다른 사용자 정보 수정이 성공한다")
        fun `Admin이 다른 사용자 정보 수정 성공`() {
            // 미리 일반 사용자 데이터 생성
            val memberUser =
                createTestUser(
                    name = "수정될사용자",
                    email = "tobeupdated@example.com",
                    password = "MemberPass123!",
                    role = UserRole.MEMBER,
                )

            // 미리 Admin 사용자 데이터 생성
            createTestUser(
                name = "Admin사용자",
                email = "adminupdate@example.com",
                password = "AdminPass123!",
                role = UserRole.ADMIN,
            )

            // Admin으로 로그인하여 토큰 획득
            val adminToken = getToken("adminupdate@example.com", "AdminPass123!")

            // Admin이 일반 사용자 정보 수정
            val updateDto =
                UserUpdateDto(
                    name = "Admin이수정한이름",
                    email = "adminupdated@example.com",
                )

            mockMvc.perform(
                put("/users/${memberUser.id}")
                    .header("Authorization", "Bearer $adminToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDto)),
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.name").value("Admin이수정한이름"))
                .andExpect(jsonPath("$.email").value("adminupdated@example.com"))
        }

        @Test
        @DisplayName("일반 사용자가 다른 사용자 정보 수정 시 권한 없음")
        fun `일반 사용자가 다른 사용자 정보 수정 시 권한 없음`() {
            // 미리 첫 번째 사용자 데이터 생성
            createTestUser(
                name = "사용자1",
                email = "user1update@example.com",
                password = "User1Pass123!",
                role = UserRole.MEMBER,
            )

            // 미리 두 번째 사용자 데이터 생성
            val user2 =
                createTestUser(
                    name = "사용자2",
                    email = "user2update@example.com",
                    password = "User2Pass123!",
                    role = UserRole.MEMBER,
                )

            // 사용자1로 로그인하여 토큰 획득
            val user1Token = getToken("user1update@example.com", "User1Pass123!")

            // 사용자1이 사용자2 정보 수정 시도 (권한 없음)
            val updateDto =
                UserUpdateDto(
                    name = "권한없이수정시도",
                    email = "unauthorized@example.com",
                )

            mockMvc.perform(
                put("/users/${user2.id}")
                    .header("Authorization", "Bearer $user1Token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDto)),
            )
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.error").value("Access Denied"))
        }

        @Test
        @DisplayName("이미 존재하는 이메일로 수정 시 실패한다")
        fun `이메일 중복으로 수정 실패`() {
            // 미리 첫 번째 사용자 데이터 생성
            val user1 =
                createTestUser(
                    name = "사용자1",
                    email = "existing1@example.com",
                    password = "User1Pass123!",
                    role = UserRole.MEMBER,
                )

            // 미리 두 번째 사용자 데이터 생성
            createTestUser(
                name = "사용자2",
                email = "existing2@example.com",
                password = "User2Pass123!",
                role = UserRole.MEMBER,
            )

            // 사용자1로 로그인하여 토큰 획득
            val user1Token = getToken("existing1@example.com", "User1Pass123!")

            // 사용자1이 사용자2의 이메일로 수정 시도
            val updateDto =
                UserUpdateDto(
                    name = "수정된이름",
                    email = "existing2@example.com",
                )

            mockMvc.perform(
                put("/users/${user1.id}")
                    .header("Authorization", "Bearer $user1Token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDto)),
            )
                .andExpect(status().isConflict)
                .andExpect(jsonPath("$.error").value("Email Already Exists"))
        }

        @Test
        @DisplayName("토큰 없이 사용자 정보 수정 시 인증 실패")
        fun `토큰 없이 사용자 정보 수정 시 인증 실패`() {
            val updateDto =
                UserUpdateDto(
                    name = "수정시도",
                    email = "update@example.com",
                )

            mockMvc.perform(
                put("/users/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDto)),
            )
                .andExpect(status().isForbidden)
        }

        @Test
        @DisplayName("존재하지 않는 사용자 정보 수정 시 실패")
        fun `존재하지 않는 사용자 정보 수정 실패`() {
            // 미리 Admin 사용자 데이터 생성
            createTestUser(
                name = "Admin사용자",
                email = "adminnonexist@example.com",
                password = "AdminPass123!",
                role = UserRole.ADMIN,
            )

            // Admin으로 로그인하여 토큰 획득
            val adminToken = getToken("adminnonexist@example.com", "AdminPass123!")

            // 존재하지 않는 사용자 수정 시도
            val updateDto =
                UserUpdateDto(
                    name = "수정시도",
                    email = "update@example.com",
                )

            mockMvc.perform(
                put("/users/999")
                    .header("Authorization", "Bearer $adminToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDto)),
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.error").value("User Not Found"))
        }

        @Test
        @DisplayName("잘못된 이메일 형식으로 수정 시 실패")
        fun `잘못된 이메일 형식으로 수정 실패`() {
            // 미리 사용자 데이터 생성
            val user =
                createTestUser(
                    name = "수정테스트사용자",
                    email = "update@example.com",
                    password = "UpdatePass123!",
                    role = UserRole.MEMBER,
                )

            // 로그인하여 토큰 획득
            val token = getToken("update@example.com", "UpdatePass123!")

            // 잘못된 이메일 형식으로 수정 시도
            val updateDto =
                UserUpdateDto(
                    name = "수정된이름",
                    email = "invalid-email",
                )

            mockMvc.perform(
                put("/users/${user.id}")
                    .header("Authorization", "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDto)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.error").value("Validation Failed"))
        }

        @Test
        @DisplayName("잘못된 이름 형식으로 수정 시 실패")
        fun `잘못된 이름 형식으로 수정 실패`() {
            // 미리 사용자 데이터 생성
            val user =
                createTestUser(
                    name = "수정테스트사용자",
                    email = "update@example.com",
                    password = "UpdatePass123!",
                    role = UserRole.MEMBER,
                )

            // 로그인하여 토큰 획득
            val token = getToken("update@example.com", "UpdatePass123!")

            // 잘못된 이름 형식으로 수정 시도
            val updateDto =
                UserUpdateDto(
                    name = "A",
                    email = "updated@example.com",
                )

            mockMvc.perform(
                put("/users/${user.id}")
                    .header("Authorization", "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDto)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.error").value("Validation Failed"))
        }

        @Test
        @DisplayName("이름에 특수문자가 포함된 수정 시 실패")
        fun `이름에 특수문자 포함 수정 실패`() {
            // 미리 사용자 데이터 생성
            val user =
                createTestUser(
                    name = "수정테스트사용자",
                    email = "update@example.com",
                    password = "UpdatePass123!",
                    role = UserRole.MEMBER,
                )

            // 로그인하여 토큰 획득
            val token = getToken("update@example.com", "UpdatePass123!")

            // 특수문자가 포함된 이름으로 수정 시도
            val updateDto =
                UserUpdateDto(
                    name = "수정된이름123",
                    email = "updated@example.com",
                )

            mockMvc.perform(
                put("/users/${user.id}")
                    .header("Authorization", "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDto)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.error").value("Validation Failed"))
        }

        @Test
        @DisplayName("빈 이름으로 수정 시 실패")
        fun `빈 이름으로 수정 실패`() {
            // 미리 사용자 데이터 생성
            val user =
                createTestUser(
                    name = "수정테스트사용자",
                    email = "update@example.com",
                    password = "UpdatePass123!",
                    role = UserRole.MEMBER,
                )

            // 로그인하여 토큰 획득
            val token = getToken("update@example.com", "UpdatePass123!")

            // 빈 이름으로 수정 시도
            val updateDto =
                UserUpdateDto(
                    name = "",
                    email = "updated@example.com",
                )

            mockMvc.perform(
                put("/users/${user.id}")
                    .header("Authorization", "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDto)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.error").value("Validation Failed"))
        }

        @Test
        @DisplayName("빈 이메일로 수정 시 실패")
        fun `빈 이메일로 수정 실패`() {
            // 미리 사용자 데이터 생성
            val user =
                createTestUser(
                    name = "수정테스트사용자",
                    email = "update@example.com",
                    password = "UpdatePass123!",
                    role = UserRole.MEMBER,
                )

            // 로그인하여 토큰 획득
            val token = getToken("update@example.com", "UpdatePass123!")

            // 빈 이메일로 수정 시도
            val updateDto =
                UserUpdateDto(
                    name = "수정된이름",
                    email = "",
                )

            mockMvc.perform(
                put("/users/${user.id}")
                    .header("Authorization", "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDto)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.error").value("Validation Failed"))
        }
    }

    @Nested
    @DisplayName("사용자 삭제 API 테스트")
    inner class DeleteUserApiTest {
        @Test
        @DisplayName("본인 계정 삭제가 성공한다")
        fun `본인 계정 삭제 성공`() {
            // 미리 사용자 데이터 생성
            val user =
                createTestUser(
                    name = "삭제테스트사용자",
                    email = "delete@example.com",
                    password = "DeletePass123!",
                    role = UserRole.MEMBER,
                )

            // 로그인하여 토큰 획득
            val token = getToken("delete@example.com", "DeletePass123!")

            // 본인 계정 삭제
            mockMvc.perform(
                delete("/users/${user.id}")
                    .header("Authorization", "Bearer $token"),
            )
                .andExpect(status().isNoContent)
        }

        @Test
        @DisplayName("Admin이 다른 사용자 계정 삭제가 성공한다")
        fun `Admin이 다른 사용자 계정 삭제 성공`() {
            // 미리 일반 사용자 데이터 생성
            val memberUser =
                createTestUser(
                    name = "삭제될사용자",
                    email = "tobedeleted@example.com",
                    password = "MemberPass123!",
                    role = UserRole.MEMBER,
                )

            // 미리 Admin 사용자 데이터 생성
            createTestUser(
                name = "Admin사용자",
                email = "admindelete@example.com",
                password = "AdminPass123!",
                role = UserRole.ADMIN,
            )

            // Admin으로 로그인하여 토큰 획득
            val adminToken = getToken("admindelete@example.com", "AdminPass123!")

            // Admin이 일반 사용자 계정 삭제
            mockMvc.perform(
                delete("/users/${memberUser.id}")
                    .header("Authorization", "Bearer $adminToken"),
            )
                .andExpect(status().isNoContent)
        }

        @Test
        @DisplayName("일반 사용자가 다른 사용자 계정 삭제 시 권한 없음")
        fun `일반 사용자가 다른 사용자 계정 삭제 시 권한 없음`() {
            // 미리 첫 번째 사용자 데이터 생성
            createTestUser(
                name = "사용자1",
                email = "user1delete@example.com",
                password = "User1Pass123!",
                role = UserRole.MEMBER,
            )

            // 미리 두 번째 사용자 데이터 생성
            val user2 =
                createTestUser(
                    name = "사용자2",
                    email = "user2delete@example.com",
                    password = "User2Pass123!",
                    role = UserRole.MEMBER,
                )

            // 사용자1로 로그인하여 토큰 획득
            val user1Token = getToken("user1delete@example.com", "User1Pass123!")

            // 사용자1이 사용자2 계정 삭제 시도 (권한 없음)
            mockMvc.perform(
                delete("/users/${user2.id}")
                    .header("Authorization", "Bearer $user1Token"),
            )
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.error").value("Access Denied"))
        }

        @Test
        @DisplayName("존재하지 않는 사용자 삭제 시 실패한다")
        fun `존재하지 않는 사용자 삭제 실패`() {
            // 미리 Admin 사용자 데이터 생성
            createTestUser(
                name = "Admin사용자",
                email = "adminnonexist@example.com",
                password = "AdminPass123!",
                role = UserRole.ADMIN,
            )

            // Admin으로 로그인하여 토큰 획득
            val adminToken = getToken("adminnonexist@example.com", "AdminPass123!")

            // 존재하지 않는 사용자 삭제 시도
            mockMvc.perform(
                delete("/users/999")
                    .header("Authorization", "Bearer $adminToken"),
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.error").value("User Not Found"))
        }

        @Test
        @DisplayName("토큰 없이 사용자 삭제 시 인증 실패")
        fun `토큰 없이 사용자 삭제 시 인증 실패`() {
            mockMvc.perform(delete("/users/1"))
                .andExpect(status().isForbidden)
        }

        @Test
        @DisplayName("잘못된 토큰으로 사용자 삭제 시 인증 실패")
        fun `잘못된 토큰으로 사용자 삭제 시 인증 실패`() {
            mockMvc.perform(
                delete("/users/1")
                    .header("Authorization", "Bearer invalid.token.here"),
            )
                .andExpect(status().isForbidden)
        }
    }

    @Nested
    @DisplayName("전체 사용자 조회 API 테스트")
    inner class GetUsersApiTest {
        @Test
        @DisplayName("Admin 권한으로 전체 사용자 조회가 성공한다")
        fun `Admin 권한으로 전체 사용자 조회 성공`() {
            // 미리 Admin 사용자 데이터 생성
            createTestUser(
                name = "Admin사용자",
                email = "adminlist@example.com",
                password = "AdminPass123!",
                role = UserRole.ADMIN,
            )

            // Admin으로 로그인하여 토큰 획득
            val adminToken = getToken("adminlist@example.com", "AdminPass123!")

            // Admin 권한으로 전체 사용자 조회
            mockMvc.perform(
                get("/users")
                    .header("Authorization", "Bearer $adminToken"),
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content").isArray)
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").exists())
                .andExpect(jsonPath("$.totalPages").exists())
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").exists())
        }

        @Test
        @DisplayName("Admin 권한으로 페이징 파라미터와 함께 사용자 조회가 성공한다")
        fun `Admin 권한으로 페이징 파라미터와 함께 사용자 조회 성공`() {
            // 미리 Admin 사용자 데이터 생성
            createTestUser(
                name = "Admin사용자",
                email = "adminpagelist@example.com",
                password = "AdminPass123!",
                role = UserRole.ADMIN,
            )

            // Admin으로 로그인하여 토큰 획득
            val adminToken = getToken("adminpagelist@example.com", "AdminPass123!")

            // Admin 권한으로 페이징 파라미터와 함께 사용자 조회
            mockMvc.perform(
                get("/users?page=0&size=5&sort=name&direction=asc")
                    .header("Authorization", "Bearer $adminToken"),
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content").isArray)
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").exists())
                .andExpect(jsonPath("$.totalPages").exists())
        }

        @Test
        @DisplayName("일반 사용자가 전체 사용자 조회 시 권한 없음")
        fun `일반 사용자가 전체 사용자 조회 시 권한 없음`() {
            // 미리 일반 사용자 데이터 생성
            createTestUser(
                name = "일반사용자",
                email = "memberlist@example.com",
                password = "MemberPass123!",
                role = UserRole.MEMBER,
            )

            // 일반 사용자로 로그인하여 토큰 획득
            val memberToken = getToken("memberlist@example.com", "MemberPass123!")

            // 일반 사용자가 전체 사용자 조회 시도
            mockMvc.perform(
                get("/users")
                    .header("Authorization", "Bearer $memberToken"),
            )
                .andExpect(status().isForbidden)
        }

        @Test
        @DisplayName("토큰 없이 전체 사용자 조회 시 인증 실패")
        fun `토큰 없이 전체 사용자 조회 시 인증 실패`() {
            mockMvc.perform(get("/users"))
                .andExpect(status().isForbidden)
        }

        @Test
        @DisplayName("잘못된 토큰으로 전체 사용자 조회 시 인증 실패")
        fun `잘못된 토큰으로 전체 사용자 조회 시 인증 실패`() {
            mockMvc.perform(
                get("/users")
                    .header("Authorization", "Bearer invalid.token.here"),
            )
                .andExpect(status().isForbidden)
        }

        @Test
        @DisplayName("잘못된 페이징 파라미터로 조회 시 기본값으로 처리된다")
        fun `잘못된 페이징 파라미터로 조회 시 기본값으로 처리`() {
            // 미리 Admin 사용자 데이터 생성
            createTestUser(
                name = "Admin사용자",
                email = "admininvalidparams@example.com",
                password = "AdminPass123!",
                role = UserRole.ADMIN,
            )

            // Admin으로 로그인하여 토큰 획득
            val adminToken = getToken("admininvalidparams@example.com", "AdminPass123!")

            // 잘못된 페이징 파라미터로 조회 시도
            mockMvc.perform(
                get("/users?page=-1&size=-5&sort=invalid&direction=invalid")
                    .header("Authorization", "Bearer $adminToken"),
            )
                .andExpect(status().isInternalServerError)
        }

        @Test
        @DisplayName("큰 페이지 번호로 조회 시 빈 결과를 반환한다")
        fun `큰 페이지 번호로 조회 시 빈 결과 반환`() {
            // 미리 Admin 사용자 데이터 생성
            createTestUser(
                name = "Admin사용자",
                email = "adminlargepage@example.com",
                password = "AdminPass123!",
                role = UserRole.ADMIN,
            )

            // Admin으로 로그인하여 토큰 획득
            val adminToken = getToken("adminlargepage@example.com", "AdminPass123!")

            // 큰 페이지 번호로 조회 시도
            mockMvc.perform(
                get("/users?page=999&size=10")
                    .header("Authorization", "Bearer $adminToken"),
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content").isArray)
                .andExpect(jsonPath("$.content").isEmpty)
                .andExpect(jsonPath("$.page").value(999))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(true))
        }
    }
}
