package com.contractnegotiation.backend.service.impl;

import com.contractnegotiation.backend.dto.NegotiationRequestDto;
import com.contractnegotiation.backend.dto.NegotiationResponseDto;
import com.contractnegotiation.backend.entity.Contract;
import com.contractnegotiation.backend.entity.Negotiation;
import com.contractnegotiation.backend.entity.NegotiationLog;
import com.contractnegotiation.backend.exception.ResourceNotFoundException;
import com.contractnegotiation.backend.repository.ContractRepository;
import com.contractnegotiation.backend.repository.NegotiationLogRepository;
import com.contractnegotiation.backend.repository.NegotiationRepository;
import com.contractnegotiation.backend.service.FastApiClientService;
import com.contractnegotiation.backend.service.NegotiationService;
import com.contractnegotiation.backend.service.S3Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NegotiationServiceImpl implements NegotiationService {

    private final ContractRepository contractRepository;
    private final NegotiationRepository negotiationRepository;
    private final NegotiationLogRepository negotiationLogRepository;
    private final S3Service s3Service;
    private final FastApiClientService fastApiClientService;
    private final ObjectMapper objectMapper;

    public NegotiationServiceImpl(
            ContractRepository contractRepository,
            NegotiationRepository negotiationRepository,
            NegotiationLogRepository negotiationLogRepository,
            S3Service s3Service,
            FastApiClientService fastApiClientService,
            ObjectMapper objectMapper) {
        this.contractRepository = contractRepository;
        this.negotiationRepository = negotiationRepository;
        this.negotiationLogRepository = negotiationLogRepository;
        this.s3Service = s3Service;
        this.fastApiClientService = fastApiClientService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public NegotiationResponseDto startNegotiation(NegotiationRequestDto requestDto) {
        // 1. Validate contract IDs
        Contract issuerContract = contractRepository.findById(requestDto.getIssuerContractId())
                .orElseThrow(() -> new ResourceNotFoundException("Issuer contract not found with id: " + requestDto.getIssuerContractId()));

        Contract acquirerContract = contractRepository.findById(requestDto.getAcquirerContractId())
                .orElseThrow(() -> new ResourceNotFoundException("Acquirer contract not found with id: " + requestDto.getAcquirerContractId()));

        // 2. Download both PDF contracts from AWS S3
        byte[] issuerBytes = s3Service.downloadFile(issuerContract.getS3Url());
        byte[] acquirerBytes = s3Service.downloadFile(acquirerContract.getS3Url());

        // 3. Send multipart request to FastAPI GenAI server
        NegotiationResponseDto responseDto = fastApiClientService.sendNegotiationRequest(
                issuerBytes, issuerContract.getOriginalFileName(),
                acquirerBytes, acquirerContract.getOriginalFileName()
        );

        // 4. Save Negotiation metadata and logs to PostgreSQL
        Negotiation negotiation = new Negotiation();
        negotiation.setIssuerContractId(issuerContract.getId());
        negotiation.setAcquirerContractId(acquirerContract.getId());
        negotiation.setStatus(responseDto.getStatus() != null ? responseDto.getStatus() : "COMPLETED");

        try {
            negotiation.setIssuerFeedback(objectMapper.writeValueAsString(responseDto.getIssuerFeedback()));
            negotiation.setAcquirerFeedback(objectMapper.writeValueAsString(responseDto.getAcquirerFeedback()));
            negotiation.setSummary(objectMapper.writeValueAsString(responseDto.getSummary()));
            negotiation.setFullAiResponseJson(objectMapper.writeValueAsString(responseDto));
        } catch (Exception e) {
            negotiation.setSummary(String.valueOf(responseDto.getSummary()));
            negotiation.setFullAiResponseJson(String.valueOf(responseDto));
        }

        Negotiation savedNegotiation = negotiationRepository.save(negotiation);

        // Save execution audit logs
        createAndSaveLog(savedNegotiation, "INFO", "Loaded issuer contract (ID: " + issuerContract.getId() + ") and acquirer contract (ID: " + acquirerContract.getId() + ")");
        createAndSaveLog(savedNegotiation, "INFO", "Downloaded PDF contract files from AWS S3 successfully.");
        createAndSaveLog(savedNegotiation, "INFO", "FastAPI GenAI negotiation execution completed with status: " + savedNegotiation.getStatus());

        return responseDto;
    }

    private void createAndSaveLog(Negotiation negotiation, String level, String message) {
        NegotiationLog log = new NegotiationLog();
        log.setNegotiation(negotiation);
        log.setLogLevel(level);
        log.setLogMessage(message);
        negotiationLogRepository.save(log);
    }
}
