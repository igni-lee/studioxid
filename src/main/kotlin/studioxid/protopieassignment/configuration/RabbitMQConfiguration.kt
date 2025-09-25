package studioxid.protopieassignment.configuration

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.ExchangeBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.QueueBuilder
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMQConfiguration {
    companion object {
        const val USER_DELETION_QUEUE = "user.deletion.queue"
        const val USER_DELETION_EXCHANGE = "user.deletion.exchange"
        const val USER_DELETION_ROUTING_KEY = "user.deletion"
        const val USER_DELETION_DLQ = "user.deletion.dlq"
        const val USER_DELETION_DLX = "user.deletion.dlx"
    }

    /**
     * JSON 메시지 컨버터
     */
    @Bean
    fun messageConverter() = Jackson2JsonMessageConverter()

    /**
     * RabbitTemplate 설정
     */
    @Bean
    fun rabbitTemplate(connectionFactory: ConnectionFactory): RabbitTemplate =
        RabbitTemplate(connectionFactory).apply {
            messageConverter = messageConverter()
        }

    /**
     * RabbitListener 컨테이너 팩토리 설정
     */
    @Bean
    fun rabbitListenerContainerFactory(connectionFactory: ConnectionFactory): SimpleRabbitListenerContainerFactory =
        SimpleRabbitListenerContainerFactory().apply {
            setConnectionFactory(connectionFactory)
            setMessageConverter(messageConverter())
            setConcurrentConsumers(3)
            setMaxConcurrentConsumers(10)
            setPrefetchCount(1)
        }

    /**
     * 사용자 삭제 큐
     */
    @Bean
    fun userDeletionQueue(): Queue =
        QueueBuilder
            .durable(USER_DELETION_QUEUE)
            .withArgument("x-dead-letter-exchange", USER_DELETION_DLX)
            .withArgument("x-dead-letter-routing-key", USER_DELETION_DLQ)
            .withArgument("x-message-ttl", 300000) // 5분 TTL
            .build()

    /**
     * 사용자 삭제 익스체인지
     */
    @Bean
    fun userDeletionExchange(): TopicExchange =
        ExchangeBuilder
            .topicExchange(USER_DELETION_EXCHANGE)
            .durable(true)
            .build()

    /**
     * 사용자 삭제 큐와 익스체인지 바인딩
     */
    @Bean
    fun userDeletionBinding(): Binding =
        BindingBuilder
            .bind(userDeletionQueue())
            .to(userDeletionExchange())
            .with(USER_DELETION_ROUTING_KEY)

    /**
     * Dead Letter Queue
     */
    @Bean
    fun userDeletionDLQ(): Queue = QueueBuilder.durable(USER_DELETION_DLQ).build()

    /**
     * Dead Letter Exchange
     */
    @Bean
    fun userDeletionDLX(): TopicExchange {
        return ExchangeBuilder.topicExchange(USER_DELETION_DLX)
            .durable(true)
            .build()
    }

    /**
     * DLQ와 DLX 바인딩
     */
    @Bean
    fun userDeletionDLQBinding(): Binding =
        BindingBuilder
            .bind(userDeletionDLQ())
            .to(userDeletionDLX())
            .with(USER_DELETION_DLQ)
}
