package studioxid.protopieassignment.service

import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service
import studioxid.protopieassignment.configuration.RabbitMQConfiguration
import studioxid.protopieassignment.entity.UserEntity
import studioxid.protopieassignment.message.DeletionTask
import studioxid.protopieassignment.message.UserDeletionMessage
import studioxid.protopieassignment.repository.UserCacheRepository
import java.time.LocalDateTime

@Service
class AsyncUserDeletionService(
    private val rabbitTemplate: RabbitTemplate,
    private val userCacheRepository: UserCacheRepository,
) {
    private val logger = LoggerFactory.getLogger(AsyncUserDeletionService::class.java)

    /**
     * 사용자 삭제 메시지를 큐에 발송
     */
    fun sendUserDeletionMessage(user: UserEntity) {
        val message =
            UserDeletionMessage(
                userId = user.id!!,
                email = user.email,
                name = user.name,
                deletionTime = LocalDateTime.now(),
            )

        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfiguration.USER_DELETION_EXCHANGE,
                RabbitMQConfiguration.USER_DELETION_ROUTING_KEY,
                message,
            )

            logger.info("사용자 삭제 메시지 발송 완료: userId={}, email={}", user.id, user.email)
        } catch (e: Exception) {
            logger.error("사용자 삭제 메시지 발송 실패: userId={}, error={}", user.id, e.message, e)
            // 메시지 발송 실패 시 동기적으로 처리
            processUserDeletionSynchronously(message)
        }
    }

    /**
     * 사용자 삭제 메시지 처리 (비동기)
     */
    @RabbitListener(queues = [RabbitMQConfiguration.USER_DELETION_QUEUE])
    fun processUserDeletion(message: UserDeletionMessage) {
        logger.info("사용자 삭제 비동기 처리 시작: userId={}, email={}", message.userId, message.email)

        try {
            processUserDeletionSynchronously(message)
            logger.info("사용자 삭제 비동기 처리 완료: userId={}", message.userId)
        } catch (e: Exception) {
            logger.error("사용자 삭제 비동기 처리 실패: userId={}, error={}", message.userId, e.message, e)
            throw e // 재시도를 위해 예외를 다시 던짐
        }
    }

    /**
     * 사용자 삭제 작업을 동기적으로 처리
     */
    private fun processUserDeletionSynchronously(message: UserDeletionMessage) {
        logger.info("사용자 삭제 작업 시작: userId={}, tasks={}", message.userId, message.tasks)

        message.tasks.forEach { task ->
            try {
                when (task) {
                    DeletionTask.CACHE_CLEANUP -> processCacheCleanup(message)
                    DeletionTask.DATA_ANONYMIZATION -> processDataAnonymization(message)
                }
                logger.info("사용자 삭제 작업 완료: userId={}, task={}", message.userId, task)
            } catch (e: Exception) {
                logger.error(
                    "사용자 삭제 작업 실패: userId={}, task={}, error={}",
                    message.userId,
                    task,
                    e.message,
                    e,
                )
                // 개별 작업 실패는 전체 프로세스를 중단하지 않음
            }
        }

        logger.info("사용자 삭제 모든 작업 완료: userId={}", message.userId)
    }

    /**
     * 캐시 정리 작업
     */
    private fun processCacheCleanup(message: UserDeletionMessage) {
        logger.info("캐시 정리 작업 시작: userId={}", message.userId)

        // 사용자 관련 캐시 삭제
        userCacheRepository.deleteUser(message.userId)
        userCacheRepository.deleteUserList()

        // 추가적인 캐시 정리 (예: 세션 캐시, 임시 데이터 등)
        logger.info("추가 캐시 정리 시뮬레이션: userId={}", message.userId)

        logger.info("캐시 정리 작업 완료: userId={}", message.userId)
    }

    /**
     * 데이터 익명화 작업
     */
    private fun processDataAnonymization(message: UserDeletionMessage) {
        logger.info("데이터 익명화 작업 시작: userId={}", message.userId)

        // 실제 구현에서는 관련 데이터를 익명화
        // 예: 로그 데이터, 분석 데이터 등에서 개인정보 제거
        logger.info("데이터 익명화 시뮬레이션: userId={}", message.userId)

        logger.info("데이터 익명화 작업 완료: userId={}", message.userId)
    }
}
