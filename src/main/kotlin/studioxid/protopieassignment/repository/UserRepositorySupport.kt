package studioxid.protopieassignment.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository
import studioxid.protopieassignment.entity.QUserEntity.userEntity
import studioxid.protopieassignment.entity.UserEntity
import java.time.LocalDateTime

@Repository
class UserRepositorySupport(
    private val queryFactory: JPAQueryFactory,
) : QuerydslRepositorySupport(UserEntity::class.java) {
    /**
     * ID로 사용자 조회 (삭제되지 않은 사용자만)
     */
    fun findByIdAndNotDeleted(id: Long): UserEntity? {
        return queryFactory
            .selectFrom(userEntity)
            .where(
                userEntity.id.eq(id),
                userEntity.deletedAt.isNull,
            )
            .fetchOne()
    }

    /**
     * 이메일로 사용자 조회 (삭제되지 않은 사용자만)
     */
    fun findByEmailAndNotDeleted(email: String): UserEntity? {
        return queryFactory
            .selectFrom(userEntity)
            .where(
                userEntity.email.eq(email),
                userEntity.deletedAt.isNull,
            )
            .fetchOne()
    }

    /**
     * 이메일 존재 여부 확인 (삭제되지 않은 사용자만)
     */
    fun existsByEmailAndNotDeleted(email: String): Boolean {
        return queryFactory
            .selectFrom(userEntity)
            .where(
                userEntity.email.eq(email),
                userEntity.deletedAt.isNull,
            )
            .fetchFirst() != null
    }

    /**
     * 모든 사용자 조회 (삭제되지 않은 사용자만, 페이징)
     */
    fun findAllNotDeleted(pageable: Pageable): Page<UserEntity> {
        val query =
            queryFactory
                .selectFrom(userEntity)
                .where(userEntity.deletedAt.isNull)
                .orderBy(userEntity.createdAt.desc())

        val total =
            queryFactory
                .selectFrom(userEntity)
                .where(userEntity.deletedAt.isNull)
                .fetchCount()

        val users =
            query
                .offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()

        return PageImpl(users, pageable, total)
    }

    /**
     * 사용자 정보 업데이트
     */
    fun update(user: UserEntity): Long {
        return queryFactory
            .update(userEntity)
            .set(userEntity.name, user.name)
            .set(userEntity.email, user.email)
            .set(userEntity.password, user.password)
            .set(userEntity.salt, user.salt)
            .set(userEntity.role, user.role)
            .set(userEntity.updatedAt, user.updatedAt)
            .where(userEntity.id.eq(user.id))
            .execute()
    }

    /**
     * 사용자 소프트 삭제
     */
    fun softDeleteById(id: Long): Long {
        return queryFactory
            .update(userEntity)
            .set(userEntity.deletedAt, LocalDateTime.now())
            .where(userEntity.id.eq(id))
            .execute()
    }
}
