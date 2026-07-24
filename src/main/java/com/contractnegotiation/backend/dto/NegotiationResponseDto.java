package com.contractnegotiation.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class NegotiationResponseDto {

    @JsonProperty("status")
    private String status;

    @JsonProperty("issuer_clauses_extracted")
    private Object issuerClausesExtracted;

    @JsonProperty("acquirer_clauses_extracted")
    private Object acquirerClausesExtracted;

    @JsonProperty("issuer_feedback")
    private Object issuerFeedback;

    @JsonProperty("acquirer_feedback")
    private Object acquirerFeedback;

    @JsonProperty("final_clauses")
    private List<FinalClauseDto> finalClauses;

    @JsonProperty("summary")
    private NegotiationSummaryDto summary;

    public NegotiationResponseDto() {
    }

    public NegotiationResponseDto(String status, Object issuerClausesExtracted, Object acquirerClausesExtracted, Object issuerFeedback, Object acquirerFeedback, List<FinalClauseDto> finalClauses, NegotiationSummaryDto summary) {
        this.status = status;
        this.issuerClausesExtracted = issuerClausesExtracted;
        this.acquirerClausesExtracted = acquirerClausesExtracted;
        this.issuerFeedback = issuerFeedback;
        this.acquirerFeedback = acquirerFeedback;
        this.finalClauses = finalClauses;
        this.summary = summary;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getIssuerClausesExtracted() {
        return issuerClausesExtracted;
    }

    public void setIssuerClausesExtracted(Object issuerClausesExtracted) {
        this.issuerClausesExtracted = issuerClausesExtracted;
    }

    public Object getAcquirerClausesExtracted() {
        return acquirerClausesExtracted;
    }

    public void setAcquirerClausesExtracted(Object acquirerClausesExtracted) {
        this.acquirerClausesExtracted = acquirerClausesExtracted;
    }

    public Object getIssuerFeedback() {
        return issuerFeedback;
    }

    public void setIssuerFeedback(Object issuerFeedback) {
        this.issuerFeedback = issuerFeedback;
    }

    public Object getAcquirerFeedback() {
        return acquirerFeedback;
    }

    public void setAcquirerFeedback(Object acquirerFeedback) {
        this.acquirerFeedback = acquirerFeedback;
    }

    public List<FinalClauseDto> getFinalClauses() {
        return finalClauses;
    }

    public void setFinalClauses(List<FinalClauseDto> finalClauses) {
        this.finalClauses = finalClauses;
    }

    public NegotiationSummaryDto getSummary() {
        return summary;
    }

    public void setSummary(NegotiationSummaryDto summary) {
        this.summary = summary;
    }
}
