package studioxid.protopieassignment.configuration

import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import java.util.Properties
import javax.sql.DataSource

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = ["studioxid.protopieassignment.repository"],
    entityManagerFactoryRef = "entityManagerFactory",
    transactionManagerRef = "transactionManager",
)
class DatabaseConfiguration {
    @Bean(name = ["writerDataSource"])
    @ConfigurationProperties(prefix = "spring.datasource.writer")
    fun writerDataSource(): DataSource {
        return DataSourceBuilder.create()
            .type(HikariDataSource::class.java)
            .build()
    }

    @Bean(name = ["readerDataSource"])
    @ConfigurationProperties(prefix = "spring.datasource.reader")
    fun readerDataSource(): DataSource {
        return DataSourceBuilder.create()
            .type(HikariDataSource::class.java)
            .build()
    }

    @Bean(name = ["entityManagerFactory"])
    @Primary
    fun entityManagerFactory(
        @Qualifier("writerDataSource") writerDataSource: DataSource,
    ): LocalContainerEntityManagerFactoryBean {
        val entityManagerFactory = LocalContainerEntityManagerFactoryBean()
        entityManagerFactory.dataSource = writerDataSource
        entityManagerFactory.setPackagesToScan("studioxid.protopieassignment.entity")
        entityManagerFactory.persistenceUnitName = "default"

        val vendorAdapter = HibernateJpaVendorAdapter()
        entityManagerFactory.jpaVendorAdapter = vendorAdapter

        // application.yml의 JPA 설정을 HibernateProperty를 통해 적용
        val jpaProperties = Properties()
        jpaProperties.setProperty("hibernate.hbm2ddl.auto", "create-drop")
        jpaProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
        jpaProperties.setProperty("hibernate.show_sql", "true")
        jpaProperties.setProperty("hibernate.format_sql", "true")
        entityManagerFactory.setJpaProperties(jpaProperties)

        return entityManagerFactory
    }

    @Bean(name = ["transactionManager"])
    @Primary
    fun transactionManager(
        @Qualifier("entityManagerFactory") entityManagerFactory: LocalContainerEntityManagerFactoryBean,
    ): PlatformTransactionManager {
        val transactionManager = JpaTransactionManager()
        transactionManager.entityManagerFactory = entityManagerFactory.`object`
        return transactionManager
    }
}
