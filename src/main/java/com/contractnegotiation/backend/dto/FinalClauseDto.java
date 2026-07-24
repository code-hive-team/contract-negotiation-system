package com.contractnegotiation.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FinalClauseDto {

    @JsonProperty("clause_key")
    private String clauseKey;

    @JsonProperty("clause_name")
    private String clauseName;

    @JsonProperty("display_type")
    private String displayType;

    @JsonProperty("issuer_value")
    private String issuerValue;

    @JsonProperty("acquirer_value")
    private String acquirerValue;

    @JsonProperty("final_value")
    private String finalValue;

    @JsonProperty("status")
    private String status;

    @JsonProperty("status_text")
    private String statusText;

    public FinalClauseDto() {
    }

    public FinalClauseDto(String clauseKey, String clauseName, String displayType, String issuerValue, String acquirerValue, String finalValue, String status, String statusText) {
        this.clauseKey = clauseKey;
        this.clauseName = clauseName;
        this.displayType = displayType;
        this.issuerValue = issuerValue;
        this.acquirerValue = acquirerValue;
        this.finalValue = finalValue;
        this.status = status;
        this.statusText = statusText;
    }

    public String getClauseKey() {
        return clauseKey;
    }

    public void setClauseKey(String clauseKey) {
        this.clauseKey = clauseKey;
    }

    public String getClauseName() {
        return clauseName;
    }

    public void setClauseName(String clauseName) {
        this.clauseName = clauseName;
    }

    public String getDisplayType() {
        return displayType;
    }

    public void setDisplayType(String displayType) {
        this.displayType = displayType;
    }

    public String getIssuerValue() {
        return issuerValue;
    }

    public void setIssuerValue(String issuerValue) {
        this.issuerValue = issuerValue;
    }

    public String getAcquirerValue() {
        return acquirerValue;
    }

    public void setAcquirerValue(String acquirerValue) {
        this.acquirerValue = acquirerValue;
    }

    public String getFinalValue() {
        return finalValue;
    }

    public void setFinalValue(String finalValue) {
        this.finalValue = finalValue;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }
}
