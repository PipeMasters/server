package com.pipemasters.server.entity;


import com.pipemasters.server.entity.enums.Role;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserTest {

    @Test
    void fullName_concatenatesFields() {
        User user = new User("Ivan", "Petrov", "Ivanovich", Collections.singleton(Role.USER), null);
        assertEquals("Petrov Ivan Ivanovich", user.getFullName());
    }
}