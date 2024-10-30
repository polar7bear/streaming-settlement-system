package com.streaming.settlement.system.settlementbatchservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.streaming.settlement.system.settlementbatchservice.repository.streaming",
        entityManagerFactoryRef = "streamingAdEntityManager",
        transactionManagerRef = "streamingAdTransactionManager"
)
public class StreamingAdDBConfig {


    @Bean(name = "streamingAdDataSource")
    @ConfigurationProperties(prefix = "spring.datasource-streaming-ad")
    public DataSource streamingAdDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean streamingAdEntityManager() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(streamingAdDataSource());
        em.setPackagesToScan("com.streaming.settlement.system.settlementbatchservice.domain.entity.streaming");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        em.setJpaPropertyMap(new HashMap<>() {{
            put("hibernate.hbm2ddl.auto", "none");
            put("hibernate.show_sql", "true");
        }});
        return em;
    }

    @Bean
    public PlatformTransactionManager streamingAdTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(streamingAdEntityManager().getObject());
        return transactionManager;
    }
}
