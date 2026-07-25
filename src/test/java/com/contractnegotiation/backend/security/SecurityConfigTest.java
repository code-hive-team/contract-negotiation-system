package com.contractnegotiation.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Bean-level unit tests for {@link SecurityConfig}. These exercise the
 * configuration class directly (without booting a full Spring context)
 * to verify the password encoder and CORS configuration it produces.
 */
@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    private static final String FRONTEND_URL = "https://frontend.example.com";

    @Mock
    private JwtAuthenticationEntryPoint authenticationEntryPoint;

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private HttpServletRequest httpServletRequest;

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig(authenticationEntryPoint, jwtAuthenticationFilter);
        ReflectionTestUtils.setField(securityConfig, "frontendUrl", FRONTEND_URL);
    }

    @Test
    void passwordEncoder_returnsBCryptEncoderThatEncodesAndMatches() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        assertTrue(encoder instanceof BCryptPasswordEncoder);

        String raw = "super-secret";
        String encoded = encoder.encode(raw);

        assertThat(encoded).isNotEqualTo(raw);
        assertTrue(encoder.matches(raw, encoded));
        assertFalse(encoder.matches("wrong-password", encoded));
    }

    @Test
    void authenticationManager_delegatesToAuthenticationConfiguration() throws Exception {
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(authenticationManager);

        AuthenticationManager result = securityConfig.authenticationManager(authenticationConfiguration);

        assertEquals(authenticationManager, result);
    }

    @Test
    void corsConfigurationSource_allowsConfiguredFrontendOrigin() {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();

        CorsConfiguration configuration = source.getCorsConfiguration(httpServletRequest);

        assertThat(configuration).isNotNull();
        assertThat(configuration.getAllowedOrigins()).containsExactly(FRONTEND_URL);
        assertThat(configuration.getAllowedMethods())
                .containsExactlyInAnyOrder("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");
        assertThat(configuration.getAllowedHeaders()).containsExactly("*");
        assertThat(configuration.getExposedHeaders())
                .containsExactlyInAnyOrder("Authorization", "Content-Disposition");
        assertTrue(configuration.getAllowCredentials());
    }
}
