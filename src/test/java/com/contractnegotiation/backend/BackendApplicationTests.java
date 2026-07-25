package com.contractnegotiation.backend;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BackendApplicationTests {

    @Test
    void contextLoads() {
        // Spring Boot context starts successfully
    }

    @Test
    void backendApplicationClassExists() {
        BackendApplication application = new BackendApplication();
        assertNotNull(application);
    }
}