package com.example.security.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class UserTest {

    @Test
    void userBuilder_shouldCreateUserCorrectly() {
        // given
        String firstname = "John";
        String lastname = "Doe";
        String email = "john.doe@example.com";
        String password = "password";
        Role role = Role.USER;

        // when
        User user = User.builder()
                .firstname(firstname)
                .lastname(lastname)
                .email(email)
                .password(password)
                .role(role)
                .build();

        // then
        assertEquals(firstname, user.getFirstname());
        assertEquals(lastname, user.getLastname());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertEquals(role, user.getRole());
    }

    @Test
    void getUsername_shouldReturnEmail() {
        // given
        String email = "john.doe@example.com";
        User user = User.builder()
                .email(email)
                .build();

        // when
        String username = user.getUsername();

        // then
        assertEquals(email, username);
    }

    @Test
    void getAuthorities_shouldReturnRoleBasedAuthorities() {
        // given
        Role role = Role.ADMIN;
        User user = User.builder()
                .role(role)
                .build();

        // when
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        // then
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority(role.name())));
    }

    @Test
    void isAccountNonExpired_shouldReturnTrue() {
        // given
        User user = new User();

        // when, then
        assertTrue(user.isAccountNonExpired());
    }

    @Test
    void isAccountNonLocked_shouldReturnTrue() {
        // given
        User user = new User();

        // when, then
        assertTrue(user.isAccountNonLocked());
    }

    @Test
    void isCredentialsNonExpired_shouldReturnTrue() {
        // given
        User user = new User();

        // when, then
        assertTrue(user.isCredentialsNonExpired());
    }

    @Test
    void isEnabled_shouldReturnTrue() {
        // given
        User user = new User();

        // when, then
        assertTrue(user.isEnabled());
    }
} 