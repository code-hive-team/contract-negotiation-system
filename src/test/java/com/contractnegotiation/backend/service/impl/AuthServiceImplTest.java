package com.contractnegotiation.backend.service.impl;

import com.contractnegotiation.backend.dto.JwtResponseDto;
import com.contractnegotiation.backend.dto.LoginRequestDto;
import com.contractnegotiation.backend.dto.RegisterRequestDto;
import com.contractnegotiation.backend.dto.UserDto;
import com.contractnegotiation.backend.entity.Role;
import com.contractnegotiation.backend.entity.User;
import com.contractnegotiation.backend.exception.ResourceNotFoundException;
import com.contractnegotiation.backend.repository.UserRepository;
import com.contractnegotiation.backend.security.JwtTokenProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthServiceImpl authService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void register_success_savesEncodedUserAndReturnsDto() {
        RegisterRequestDto request = new RegisterRequestDto("jane", "jane@example.com", "secret1", null);

        when(userRepository.existsByUsername("jane")).thenReturn(false);
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret1")).thenReturn("encoded-secret");

        User saved = new User();
        saved.setId(10L);
        saved.setUsername("jane");
        saved.setEmail("jane@example.com");
        saved.setRole(Role.ROLE_USER);
        saved.setCreatedAt(LocalDateTime.now());

        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserDto result = authService.register(request);

        assertEquals(10L, result.getId());
        assertEquals("jane", result.getUsername());
        assertEquals(Role.ROLE_USER, result.getRole());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("encoded-secret", captor.getValue().getPassword());
        assertEquals(Role.ROLE_USER, captor.getValue().getRole());
    }

    @Test
    void register_explicitRole_isPreserved() {
        RegisterRequestDto request = new RegisterRequestDto("admin", "admin@example.com", "secret1", Role.ROLE_ADMIN);

        when(userRepository.existsByUsername("admin")).thenReturn(false);
        when(userRepository.existsByEmail("admin@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret1")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = authService.register(request);

        assertEquals(Role.ROLE_ADMIN, result.getRole());
    }

    @Test
    void register_usernameTaken_throwsIllegalArgumentException() {
        RegisterRequestDto request = new RegisterRequestDto("jane", "jane@example.com", "secret1", null);

        when(userRepository.existsByUsername("jane")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.register(request));

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_emailTaken_throwsIllegalArgumentException() {
        RegisterRequestDto request = new RegisterRequestDto("jane", "jane@example.com", "secret1", null);

        when(userRepository.existsByUsername("jane")).thenReturn(false);
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.register(request));

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_success_returnsJwtResponse() {
        LoginRequestDto request = new LoginRequestDto("jane", "secret1");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt-token");

        User user = new User();
        user.setId(10L);
        user.setUsername("jane");
        user.setEmail("jane@example.com");
        user.setRole(Role.ROLE_USER);

        when(userRepository.findByUsername("jane")).thenReturn(Optional.of(user));

        JwtResponseDto response = authService.login(request);

        assertEquals("jwt-token", response.getToken());
        assertEquals(10L, response.getId());
        assertEquals("jane", response.getUsername());
        assertEquals(Role.ROLE_USER, response.getRole());

        assertEquals(authentication, SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void login_userMissingAfterAuthentication_throwsResourceNotFoundException() {
        LoginRequestDto request = new LoginRequestDto("ghost", "secret1");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt-token");
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.login(request));
    }
}
