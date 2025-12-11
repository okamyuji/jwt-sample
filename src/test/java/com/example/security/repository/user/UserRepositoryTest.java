package com.example.security.repository.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.example.security.model.Role;
import com.example.security.model.User;

@SuppressWarnings("null")
@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByEmail_shouldReturnUser_whenUserExists() {
        // given
        String email = "john.doe@example.com";
        User user = User.builder()
                .firstname("John")
                .lastname("Doe")
                .email(email)
                .password("password")
                .role(Role.USER)
                .build();

        entityManager.persist(user);
        entityManager.flush();

        // when
        Optional<User> foundUser = userRepository.findByEmail(email);

        // then
        assertTrue(foundUser.isPresent());
        assertEquals(email, foundUser.get().getEmail());
    }

    @Test
    void findByEmail_shouldReturnEmptyOptional_whenUserDoesNotExist() {
        // given
        String email = "non.existent@example.com";

        // when
        Optional<User> foundUser = userRepository.findByEmail(email);

        // then
        assertTrue(foundUser.isEmpty());
    }

    @Test
    void save_shouldPersistAndRetrieveUser() {
        // given
        User user = User.builder()
                .firstname("Jane")
                .lastname("Smith")
                .email("jane.smith@example.com")
                .password("password")
                .role(Role.ADMIN)
                .build();

        // when
        User savedUser = userRepository.save(user);
        User retrievedUser = entityManager.find(User.class, savedUser.getId());

        // then
        assertEquals(user.getFirstname(), retrievedUser.getFirstname());
        assertEquals(user.getLastname(), retrievedUser.getLastname());
        assertEquals(user.getEmail(), retrievedUser.getEmail());
        assertEquals(user.getPassword(), retrievedUser.getPassword());
        assertEquals(user.getRole(), retrievedUser.getRole());
    }
}