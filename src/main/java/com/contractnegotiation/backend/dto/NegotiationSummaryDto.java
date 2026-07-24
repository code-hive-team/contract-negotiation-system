package com.contractnegotiation.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NegotiationSummaryDto {

    @JsonProperty("clauses_agreed")
    private Integer clausesAgreed;

    @JsonProperty("clauses_modified")
    private Integer clausesModified;

    @JsonProperty("clauses_rejected")
    private Integer clausesRejected;

    @JsonProperty("negotiation_status")
    private String negotiationStatus;

    public NegotiationSummaryDto() {
    }

    public NegotiationSummaryDto(Integer clausesAgreed, Integer clausesModified, Integer clausesRejected, String negotiationStatus) {
        this.clausesAgreed = clausesAgreed;
        this.clausesModified = clausesModified;
        this.clausesRejected = clausesRejected;
        this.negotiationStatus = negotiationStatus;
    }

    public Integer getClausesAgreed() {
        return clausesAgreed;
    }

    public void setClausesAgreed(Integer clausesAgreed) {
        this.clausesAgreed = clausesAgreed;
    }

    public Integer getClausesModified() {
        return clausesModified;
    }

    public void setClausesModified(Integer clausesModified) {
        this.clausesModified = clausesModified;
    }

    public Integer getClausesRejected() {
        return clausesRejected;
    }

    public void setClausesRejected(Integer clausesRejected) {
        this.clausesRejected = clausesRejected;
    }

    public String getNegotiationStatus() {
        return negotiationStatus;
    }

    public void setNegotiationStatus(String negotiationStatus) {
        this.negotiationStatus = negotiationStatus;
    }
}
