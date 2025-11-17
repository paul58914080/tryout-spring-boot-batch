package edu.tryoutspringbootbatch.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan({"edu.tryoutspringbootbatch.entity"})
@EnableJpaRepositories(basePackages = {"edu.tryoutspringbootbatch.repository"})
public class JpaConfig {

}
