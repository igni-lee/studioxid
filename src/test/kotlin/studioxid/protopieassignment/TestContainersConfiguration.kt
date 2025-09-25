package studioxid.protopieassignment

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
abstract class TestContainersConfiguration {
    companion object {
        @Container
        @JvmStatic
        val postgresContainer: PostgreSQLContainer<*> =
            PostgreSQLContainer<Nothing>("postgres:15-alpine")
                .withDatabaseName("protopie_assignment")

        @Container
        @JvmStatic
        val rabbitMQContainer: RabbitMQContainer = RabbitMQContainer("rabbitmq:3.12-management-alpine")

        @Container
        @JvmStatic
        val redisContainer: GenericContainer<*> =
            GenericContainer<Nothing>(DockerImageName.parse("redis:7-alpine"))
                .withExposedPorts(6379)

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            // PostgreSQL 설정
            registry.add("spring.datasource.writer.jdbc-url") {
                postgresContainer.jdbcUrl
            }
            registry.add("spring.datasource.writer.username") {
                postgresContainer.username
            }
            registry.add("spring.datasource.writer.password") {
                postgresContainer.password
            }
            registry.add("spring.datasource.reader.jdbc-url") {
                postgresContainer.jdbcUrl
            }
            registry.add("spring.datasource.reader.username") {
                postgresContainer.username
            }
            registry.add("spring.datasource.reader.password") {
                postgresContainer.password
            }

            // RabbitMQ 설정
            registry.add("spring.rabbitmq.host") {
                rabbitMQContainer.host
            }
            registry.add("spring.rabbitmq.port") {
                rabbitMQContainer.amqpPort
            }
            registry.add("spring.rabbitmq.username") {
                "guest"
            }
            registry.add("spring.rabbitmq.password") {
                "guest"
            }

            // Redis 설정
            registry.add("spring.data.redis.host") {
                redisContainer.host
            }
            registry.add("spring.data.redis.port") {
                redisContainer.getMappedPort(6379)
            }
        }
    }
}
