package com.example.security.controller.token;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.security.service.jwt.JwtService;
import com.example.security.service.jwt.JwtService.JwtToken;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class TokenRefreshControllerTest {

    private MockMvc mockMvc;

    @Mock
    private JwtService jwtService;

    @Mock
    private JwtDecoder jwtDecoder;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private TokenRefreshController controller;

    private ObjectMapper objectMapper = new ObjectMapper();

    private static final String REFRESH_TOKEN = "refreshToken";
    private static final String ACCESS_TOKEN = "newAccessToken";
    private static final String USERNAME = "user@example.com";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    void refreshToken_withValidToken_shouldReturnNewTokens() throws Exception {
        // given
        Jwt jwt = Jwt.withTokenValue(REFRESH_TOKEN)
                .header("alg", "RS256")
                .subject(USERNAME)
                .build();

        UserDetails userDetails = User.withUsername(USERNAME)
                .password("password")
                .authorities("ROLE_USER")
                .build();

        JwtToken jwtToken = new JwtToken(ACCESS_TOKEN, REFRESH_TOKEN, null);

        when(jwtDecoder.decode(REFRESH_TOKEN)).thenReturn(jwt);
        when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn(jwtToken);

        // when
        MvcResult result = mockMvc.perform(post("/api/v1/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + REFRESH_TOKEN))
                .andExpect(status().isOk())
                .andReturn();

        // then
        TokenRefreshController.TokenRefreshResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                TokenRefreshController.TokenRefreshResponse.class);

        assertEquals(ACCESS_TOKEN, response.accessToken());
        assertEquals(REFRESH_TOKEN, response.refreshToken());
    }

    @Test
    void refreshToken_withInvalidToken_shouldReturnUnauthorized() throws Exception {
        // given
        when(jwtDecoder.decode(anyString())).thenThrow(new JwtException("Invalid token"));

        // when, then
        mockMvc.perform(post("/api/v1/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer invalidToken"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshToken_withMissingAuthHeader_shouldReturnBadRequest() throws Exception {
        // when, then
        mockMvc.perform(post("/api/v1/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refreshToken_withInvalidAuthHeader_shouldReturnBadRequest() throws Exception {
        // when, then
        mockMvc.perform(post("/api/v1/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Invalid"))
                .andExpect(status().isBadRequest());
    }
} 