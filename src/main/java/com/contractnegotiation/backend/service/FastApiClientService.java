package com.contractnegotiation.backend.service;

import com.contractnegotiation.backend.dto.NegotiationResponseDto;

public interface FastApiClientService {

    NegotiationResponseDto sendNegotiationRequest(byte[] issuerPdfBytes, String issuerFilename, byte[] acquirerPdfBytes, String acquirerFilename);
}
