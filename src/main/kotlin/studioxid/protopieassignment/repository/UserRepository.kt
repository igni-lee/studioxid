package studioxid.protopieassignment.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import studioxid.protopieassignment.entity.UserEntity

@Repository
interface UserRepository : JpaRepository<UserEntity, Long>
