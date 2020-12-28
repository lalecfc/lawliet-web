package com.gmail.leonard.spring;

import com.gmail.leonard.spring.syncserver.SyncManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import java.sql.SQLException;

/**
 * The entry point of the Spring Boot application.
 */
@SpringBootApplication
public class Application extends SpringBootServletInitializer {

    private final static Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws SQLException {
        SpringApplication.run(Application.class, args);
        LOGGER.info("###########################");
        Console.getInstance().start();
        SyncManager.getInstance().start();
    }

}