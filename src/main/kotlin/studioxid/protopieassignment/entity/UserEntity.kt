package studioxid.protopieassignment.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import studioxid.protopieassignment.constant.UserRole
import studioxid.protopieassignment.dto.UserDto

@Table(
    name = "users",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["email"]),
    ],
)
@Entity
class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(name = "name", nullable = false, length = 100)
    var name: String,
    @Column(name = "email", nullable = false, unique = true, length = 255)
    var email: String,
    @Column(name = "password", nullable = false, length = 255)
    var password: String,
    @Column(name = "salt", nullable = false, length = 255)
    var salt: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    var role: UserRole,
) : BaseEntity() {
    fun toUserDto() =
        UserDto(
            id = id!!,
            name = name,
            email = email,
            role = role,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
