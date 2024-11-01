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
        basePackages = "com.streaming.settlement.system.settlementbatchservice.repository.settlement",
        entityManagerFactoryRef = "settlementStatisticsEntityManager",
        transactionManagerRef = "settlementStatisticsTransactionManager"
)
public class SettlementStatisticsDBConfig {

    @Bean(name = "settlementStatisticsDataSource")
    @ConfigurationProperties(prefix = "spring.datasource-settlement-statistics")
    public DataSource settlementStatisticsDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean settlementStatisticsEntityManager() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(settlementStatisticsDataSource());
        em.setPackagesToScan("com.streaming.settlement.system.settlementbatchservice.domain.entity.settlement");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        em.setJpaPropertyMap(new HashMap<>() {{
            put("hibernate.physical_naming_strategy",
                    "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
            put("hibernate.implicit_naming_strategy",
                    "org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy");
            put("hibernate.hbm2ddl.auto", "none");
            put("hibernate.show_sql", "true");
        }});
        return em;
    }

    @Bean
    public PlatformTransactionManager settlementStatisticsTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(settlementStatisticsEntityManager().getObject());
        return transactionManager;
    }
}
