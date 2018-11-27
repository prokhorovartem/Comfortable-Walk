package com.netcracker.routebuilder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan({"com.netcracker.datacollector.*", "com.netcracker.routebuilder.*"})
@EnableJpaRepositories(basePackages = {"com.netcracker.datacollector.data.*", "com.netcracker.routebuilder.*"})
@EntityScan("com.netcracker.datacollector.data.*")
@EnableScheduling
public class RouteBuilderApplication {

    public static void main(String[] args) {
        SpringApplication.run(RouteBuilderApplication.class, args);
    }

}
