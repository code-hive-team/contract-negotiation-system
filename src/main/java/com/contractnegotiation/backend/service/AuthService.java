package com.contractnegotiation.backend.service;

import com.contractnegotiation.backend.dto.JwtResponseDto;
import com.contractnegotiation.backend.dto.LoginRequestDto;
import com.contractnegotiation.backend.dto.RegisterRequestDto;
import com.contractnegotiation.backend.dto.UserDto;

public interface AuthService {

    UserDto register(RegisterRequestDto registerRequestDto);

    JwtResponseDto login(LoginRequestDto loginRequestDto);
}
