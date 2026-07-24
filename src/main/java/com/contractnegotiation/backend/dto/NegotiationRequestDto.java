package com.contractnegotiation.backend.dto;

import jakarta.validation.constraints.NotNull;

public class NegotiationRequestDto {

    @NotNull(message = "issuerContractId is required")
    private Long issuerContractId;

    @NotNull(message = "acquirerContractId is required")
    private Long acquirerContractId;

    public NegotiationRequestDto() {
    }

    public NegotiationRequestDto(Long issuerContractId, Long acquirerContractId) {
        this.issuerContractId = issuerContractId;
        this.acquirerContractId = acquirerContractId;
    }

    public Long getIssuerContractId() {
        return issuerContractId;
    }

    public void setIssuerContractId(Long issuerContractId) {
        this.issuerContractId = issuerContractId;
    }

    public Long getAcquirerContractId() {
        return acquirerContractId;
    }

    public void setAcquirerContractId(Long acquirerContractId) {
        this.acquirerContractId = acquirerContractId;
    }
}
