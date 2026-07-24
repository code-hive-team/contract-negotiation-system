package com.contractnegotiation.backend.controller;

import com.contractnegotiation.backend.dto.ApiResponseDto;
import com.contractnegotiation.backend.dto.JwtResponseDto;
import com.contractnegotiation.backend.dto.LoginRequestDto;
import com.contractnegotiation.backend.dto.RegisterRequestDto;
import com.contractnegotiation.backend.dto.UserDto;
import com.contractnegotiation.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponseDto<UserDto>> registerUser(@Valid @RequestBody RegisterRequestDto registerRequestDto) {
        UserDto userDto = authService.register(registerRequestDto);
        ApiResponseDto<UserDto> response = new ApiResponseDto<>(
                HttpStatus.CREATED.value(),
                "User registered successfully",
                userDto
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<JwtResponseDto>> authenticateUser(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        JwtResponseDto jwtResponse = authService.login(loginRequestDto);
        ApiResponseDto<JwtResponseDto> response = new ApiResponseDto<>(
                HttpStatus.OK.value(),
                "Login successful",
                jwtResponse
        );
        return ResponseEntity.ok(response);
    }
}
