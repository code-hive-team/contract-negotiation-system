package com.contractnegotiation.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "negotiation_logs")
public class NegotiationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negotiation_id", nullable = false)
    private Negotiation negotiation;

    @Column(name = "log_message", columnDefinition = "TEXT", nullable = false)
    private String logMessage;

    @Column(name = "log_level", nullable = false)
    private String logLevel;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    public NegotiationLog() {
    }

    public NegotiationLog(Long id, Negotiation negotiation, String logMessage, String logLevel, LocalDateTime timestamp) {
        this.id = id;
        this.negotiation = negotiation;
        this.logMessage = logMessage;
        this.logLevel = logLevel;
        this.timestamp = timestamp;
    }

    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Negotiation getNegotiation() {
        return negotiation;
    }

    public void setNegotiation(Negotiation negotiation) {
        this.negotiation = negotiation;
    }

    public String getLogMessage() {
        return logMessage;
    }

    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
