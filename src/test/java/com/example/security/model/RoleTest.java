package com.example.security.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RoleTest {

    @Test
    void enum_shouldHaveCorrectValues() {
        // when, then
        assertEquals(2, Role.values().length);
        assertEquals("USER", Role.USER.name());
        assertEquals("ADMIN", Role.ADMIN.name());
    }

    @Test
    void valueOf_shouldReturnCorrectEnum() {
        // when, then
        assertEquals(Role.USER, Role.valueOf("USER"));
        assertEquals(Role.ADMIN, Role.valueOf("ADMIN"));
    }
    
    @Test
    void ordinal_shouldHaveCorrectOrder() {
        // when, then
        assertEquals(0, Role.USER.ordinal());
        assertEquals(1, Role.ADMIN.ordinal());
    }
} 