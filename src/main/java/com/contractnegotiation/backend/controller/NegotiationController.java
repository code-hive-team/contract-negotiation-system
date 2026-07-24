package com.contractnegotiation.backend.controller;

import com.contractnegotiation.backend.dto.ApiResponseDto;
import com.contractnegotiation.backend.dto.NegotiationRequestDto;
import com.contractnegotiation.backend.dto.NegotiationResponseDto;
import com.contractnegotiation.backend.entity.Negotiation;
import com.contractnegotiation.backend.repository.NegotiationRepository;
import com.contractnegotiation.backend.service.NegotiationService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/negotiations")
public class NegotiationController {

    private final NegotiationService negotiationService;
    private final NegotiationRepository negotiationRepository;


    public NegotiationController(
            NegotiationService negotiationService,
            NegotiationRepository negotiationRepository) {

        this.negotiationService = negotiationService;
        this.negotiationRepository = negotiationRepository;
    }


    // Start AI negotiation
    @PostMapping("/start")
    public ResponseEntity<ApiResponseDto<NegotiationResponseDto>> startNegotiation(
            @Valid @RequestBody NegotiationRequestDto negotiationRequestDto) {


        NegotiationResponseDto result =
                negotiationService.startNegotiation(negotiationRequestDto);


        ApiResponseDto<NegotiationResponseDto> response =
                new ApiResponseDto<>(
                        HttpStatus.OK.value(),
                        "Negotiation orchestration completed successfully",
                        result
                );


        return ResponseEntity.ok(response);
    }



    // Get negotiation by ID
    @GetMapping("/{id}")
    public ResponseEntity<Negotiation> getNegotiationById(
            @PathVariable Long id) {


        Negotiation negotiation =
                negotiationRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Negotiation not found with id: " + id
                                ));


        return ResponseEntity.ok(negotiation);
    }



    // Get all negotiations
    @GetMapping
    public ResponseEntity<List<Negotiation>> getAllNegotiations() {

        return ResponseEntity.ok(
                negotiationRepository.findAll()
        );
    }
}