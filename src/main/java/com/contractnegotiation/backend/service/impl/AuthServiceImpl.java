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
import com.contractnegotiation.backend.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional
    public UserDto register(RegisterRequestDto registerRequestDto) {

        boolean usernameExists = userRepository.existsByUsername(registerRequestDto.getUsername());
        if (usernameExists) {
            throw new IllegalArgumentException(
                    "Username '" + registerRequestDto.getUsername() + "' is already taken");
        }

        boolean emailExists = userRepository.existsByEmail(registerRequestDto.getEmail());
        if (emailExists) {
            throw new IllegalArgumentException(
                    "Email '" + registerRequestDto.getEmail() + "' is already in use");
        }

        User user = new User();
        user.setUsername(registerRequestDto.getUsername());
        user.setEmail(registerRequestDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequestDto.getPassword()));

        Role role = registerRequestDto.getRole() == null
                ? Role.ROLE_USER
                : registerRequestDto.getRole();

        user.setRole(role);

        User savedUser = userRepository.save(user);

        return mapToDto(savedUser);
    }

    @Override
    public JwtResponseDto login(LoginRequestDto loginRequestDto) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getUsername(),
                        loginRequestDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(authentication);

        User user = userRepository.findByUsername(loginRequestDto.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with username: " + loginRequestDto.getUsername()));

        return new JwtResponseDto(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole());
    }

    private UserDto mapToDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt());
    }
}