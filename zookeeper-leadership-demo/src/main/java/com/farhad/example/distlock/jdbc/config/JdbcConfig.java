package com.farhad.example.distlock.jdbc.config;

import javax.sql.DataSource;
import org.springframework.integration.jdbc.lock.DefaultLockRepository;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.integration.jdbc.lock.LockRepository;

import com.farhad.example.distlock.common.Constants;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile(Constants.JDBC_PROFILE)
public class JdbcConfig {
    
    @Bean
    @Profile(Constants.JDBC_PROFILE)
    public DefaultLockRepository  defaultLockRepository(DataSource dataSource) {
        return new DefaultLockRepository(dataSource);
    }

    @Bean
    @Profile(Constants.JDBC_PROFILE)
    public JdbcLockRegistry jdbcLockRegistry(LockRepository lockRepository) {
        return new JdbcLockRegistry(lockRepository);

    }
}
