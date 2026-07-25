package com.contractnegotiation.backend.security;

import com.contractnegotiation.backend.controller.AuthController;
import com.contractnegotiation.backend.repository.UserRepository;
import com.contractnegotiation.backend.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Boots only the web layer (no JPA/datasource) with the real
 * {@link SecurityConfig} filter chain wired in, to verify routing rules
 * (public vs. protected endpoints) and CORS behavior end-to-end.
 */
@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class,
        JwtTokenProvider.class, CustomUserDetailsService.class})
@TestPropertySource(properties = {
        "app.frontend.url=https://frontend.example.com",
        "app.jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
        "app.jwt.expiration-milliseconds=86400000"
})
class SecurityFilterChainIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void publicAuthEndpoint_isAccessibleWithoutAuthentication() throws Exception {
        // /api/auth/** is permitAll; a GET here is unauthenticated but should not
        // be rejected by the security layer with 401 (it 405s at the MVC layer instead,
        // proving the request passed through security).
        mockMvc.perform(get("/api/auth/login"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.junit.jupiter.api.Assertions.assertNotEquals(401, status);
                });
    }

    @Test
    void swaggerEndpoint_isPermittedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.junit.jupiter.api.Assertions.assertNotEquals(401, status);
                });
    }

    @Test
    void protectedEndpoint_withoutAuthentication_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/contracts/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedNegotiationsEndpoint_withoutAuthentication_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/negotiations/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unmatchedEndpoint_withoutAuthentication_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/some-other-resource"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void preflightRequest_isPermittedAndReturnsConfiguredCorsHeaders() throws Exception {
        mockMvc.perform(options("/api/contracts/1")
                        .header(HttpHeaders.ORIGIN, "https://frontend.example.com")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET.name()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://frontend.example.com"));
    }
}
