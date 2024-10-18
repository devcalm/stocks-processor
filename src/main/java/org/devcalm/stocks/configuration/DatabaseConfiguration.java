package org.devcalm.stocks.configuration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfiguration {

    @Bean(initMethod = "migrate")
    public Flyway migration(DataSource dataSource) {
        FluentConfiguration fluentConfiguration = Flyway.configure()
                .baselineOnMigrate(true)
                .dataSource(dataSource);
        return new Flyway(fluentConfiguration);
    }
}
