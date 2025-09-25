package studioxid.protopieassignment.message

import java.time.LocalDateTime

/**
 * 사용자 삭제 시 비동기 처리할 작업을 담는 메시지
 */
data class UserDeletionMessage(
    val userId: Long,
    val email: String,
    val name: String,
    val deletionTime: LocalDateTime = LocalDateTime.now(),
    val tasks: List<DeletionTask> =
        listOf(
            DeletionTask.CACHE_CLEANUP,
            DeletionTask.DATA_ANONYMIZATION,
        ),
)

/**
 * 삭제 작업 유형
 */
enum class DeletionTask {
    CACHE_CLEANUP, // 관련 캐시 정리
    DATA_ANONYMIZATION, // 개인정보 익명화 (필요시)
}
