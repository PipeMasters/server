package com.pipemasters.server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(initializers = TestEnvInitializer.class)
class ServerApplicationTests {

    @Test
    void contextLoads() {
        System.out.print("Hello World!");
    }

}
