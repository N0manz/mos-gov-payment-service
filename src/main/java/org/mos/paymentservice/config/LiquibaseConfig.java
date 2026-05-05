package org.mos.paymentservice.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties(LiquibaseProperties.class)
public class LiquibaseConfig {

    private final LiquibaseProperties properties;

    public LiquibaseConfig(LiquibaseProperties properties) {
        this.properties = properties;
    }

    @Bean
    public DataSource liquibaseDataSource(
            org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties r2dbcProperties) {
        String jdbcUrl = r2dbcProperties.getUrl()
                .replace("r2dbc:postgresql", "jdbc:postgresql")
                .replace("r2dbc:pool:postgresql", "jdbc:postgresql");

        var dataSource = new DriverManagerDataSource();
        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(r2dbcProperties.getUsername());
        dataSource.setPassword(r2dbcProperties.getPassword());
        dataSource.setDriverClassName("org.postgresql.Driver");
        return dataSource;
    }

    @Bean
    public SpringLiquibase liquibase(DataSource liquibaseDataSource) {
        var liquibase = new SpringLiquibase();
        liquibase.setDataSource(liquibaseDataSource);
        liquibase.setChangeLog(properties.getChangeLog());
        liquibase.setShouldRun(properties.isEnabled());
        return liquibase;
    }
}
