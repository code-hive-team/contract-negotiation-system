package com.contractnegotiation.backend.service;

import com.contractnegotiation.backend.dto.NegotiationRequestDto;
import com.contractnegotiation.backend.dto.NegotiationResponseDto;

public interface NegotiationService {

    NegotiationResponseDto startNegotiation(NegotiationRequestDto requestDto);
}
