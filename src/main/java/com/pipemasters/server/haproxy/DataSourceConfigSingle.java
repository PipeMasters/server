package com.pipemasters.server.haproxy;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Profile({"default", "dev"})
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories("com.pipemasters.server.repository")
public class DataSourceConfigSingle { }
