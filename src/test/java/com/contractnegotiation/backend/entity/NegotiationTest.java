package com.contractnegotiation.backend.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class NegotiationTest {

    @Test
    void gettersAndSetters_roundTripAllFields() {
        Negotiation negotiation = new Negotiation();
        LocalDateTime created = LocalDateTime.now().minusDays(1);
        LocalDateTime updated = LocalDateTime.now();

        negotiation.setId(1L);
        negotiation.setIssuerContractId(10L);
        negotiation.setAcquirerContractId(20L);
        negotiation.setStatus("IN_PROGRESS");
        negotiation.setIssuerFeedback("issuer feedback");
        negotiation.setAcquirerFeedback("acquirer feedback");
        negotiation.setSummary("summary text");
        negotiation.setFullAiResponseJson("{\"key\":\"value\"}");
        negotiation.setCreatedAt(created);
        negotiation.setUpdatedAt(updated);

        assertThat(negotiation.getId()).isEqualTo(1L);
        assertThat(negotiation.getIssuerContractId()).isEqualTo(10L);
        assertThat(negotiation.getAcquirerContractId()).isEqualTo(20L);
        assertThat(negotiation.getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(negotiation.getIssuerFeedback()).isEqualTo("issuer feedback");
        assertThat(negotiation.getAcquirerFeedback()).isEqualTo("acquirer feedback");
        assertThat(negotiation.getSummary()).isEqualTo("summary text");
        assertThat(negotiation.getFullAiResponseJson()).isEqualTo("{\"key\":\"value\"}");
        assertThat(negotiation.getCreatedAt()).isEqualTo(created);
        assertThat(negotiation.getUpdatedAt()).isEqualTo(updated);
    }

    @Test
    void onCreate_setsCreatedAtAndUpdatedAt_whenCreatedAtIsNull() {
        Negotiation negotiation = new Negotiation();

        negotiation.onCreate();

        assertThat(negotiation.getCreatedAt()).isNotNull();
        assertThat(negotiation.getUpdatedAt()).isNotNull();
        assertThat(negotiation.getCreatedAt()).isEqualTo(negotiation.getUpdatedAt());
    }

    @Test
    void onCreate_doesNotOverwriteExistingCreatedAt() {
        Negotiation negotiation = new Negotiation();
        LocalDateTime original = LocalDateTime.now().minusDays(5);
        negotiation.setCreatedAt(original);

        negotiation.onCreate();

        assertThat(negotiation.getCreatedAt()).isEqualTo(original);
        assertThat(negotiation.getUpdatedAt()).isNotNull();
    }

    @Test
    void onUpdate_refreshesUpdatedAtTimestamp() {
        Negotiation negotiation = new Negotiation();
        negotiation.setUpdatedAt(LocalDateTime.now().minusDays(1));

        negotiation.onUpdate();

        assertThat(negotiation.getUpdatedAt()).isNotNull();
        assertThat(negotiation.getUpdatedAt()).isAfter(LocalDateTime.now().minusMinutes(1));
    }
}
