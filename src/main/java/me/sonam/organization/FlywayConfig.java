package me.sonam.organization;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.flyway.enabled", havingValue = "true", matchIfMissing = true)
public class FlywayConfig {
    @Bean(initMethod = "migrate")
    Flyway flyway(@Value("${spring.datasource.url}") String url,
                  @Value("${spring.datasource.username}") String username,
                  @Value("${spring.datasource.password}") String password) {
        return Flyway.configure()
                .dataSource(url, username, password)
                .baselineOnMigrate(true)
                .locations("classpath:db/migration")
                .load();
    }
}
