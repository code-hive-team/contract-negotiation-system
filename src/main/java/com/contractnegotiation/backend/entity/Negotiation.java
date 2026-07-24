package com.contractnegotiation.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "negotiations")
public class Negotiation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "issuer_contract_id", nullable = false)
    private Long issuerContractId;

    @Column(name = "acquirer_contract_id", nullable = false)
    private Long acquirerContractId;

    @Column(nullable = false)
    private String status;

    @Column(name = "issuer_feedback", columnDefinition = "TEXT")
    private String issuerFeedback;

    @Column(name = "acquirer_feedback", columnDefinition = "TEXT")
    private String acquirerFeedback;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "full_ai_response_json", columnDefinition = "TEXT")
    private String fullAiResponseJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Negotiation() {
    }

    public Negotiation(Long id, Long issuerContractId, Long acquirerContractId, String status, String issuerFeedback, String acquirerFeedback, String summary, String fullAiResponseJson, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.issuerContractId = issuerContractId;
        this.acquirerContractId = acquirerContractId;
        this.status = status;
        this.issuerFeedback = issuerFeedback;
        this.acquirerFeedback = acquirerFeedback;
        this.summary = summary;
        this.fullAiResponseJson = fullAiResponseJson;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIssuerFeedback() {
        return issuerFeedback;
    }

    public void setIssuerFeedback(String issuerFeedback) {
        this.issuerFeedback = issuerFeedback;
    }

    public String getAcquirerFeedback() {
        return acquirerFeedback;
    }

    public void setAcquirerFeedback(String acquirerFeedback) {
        this.acquirerFeedback = acquirerFeedback;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getFullAiResponseJson() {
        return fullAiResponseJson;
    }

    public void setFullAiResponseJson(String fullAiResponseJson) {
        this.fullAiResponseJson = fullAiResponseJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
