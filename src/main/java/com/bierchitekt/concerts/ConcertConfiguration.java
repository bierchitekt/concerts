package com.bierchitekt.concerts;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.Objects;

@Configuration
@RequiredArgsConstructor
public class ConcertConfiguration {

    private final Environment env;

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(Objects.requireNonNull(this.env.getProperty("driverClassName")));
        dataSource.setUrl(this.env.getProperty("url"));
        dataSource.setUsername(this.env.getProperty("user"));
        dataSource.setPassword(this.env.getProperty("password"));
        return dataSource;
    }
}
