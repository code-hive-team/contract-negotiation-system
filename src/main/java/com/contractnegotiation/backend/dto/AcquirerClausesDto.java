package com.contractnegotiation.backend.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class AcquirerClausesDto {

    @JsonProperty("payment_terms")
    private Object paymentTerms;

    @JsonProperty("delivery")
    private Object delivery;

    @JsonProperty("warranty")
    private Object warranty;

    @JsonProperty("confidentiality")
    private Object confidentiality;

    @JsonProperty("intellectual_property")
    private Object intellectualProperty;

    @JsonProperty("liability")
    private Object liability;

    @JsonProperty("indemnification")
    private Object indemnification;

    @JsonProperty("late_payment_penalty")
    private Object latePaymentPenalty;

    @JsonProperty("termination")
    private Object termination;

    @JsonProperty("governing_law")
    private Object governingLaw;

    @JsonProperty("force_majeure")
    private Object forceMajeure;

    private Map<String, Object> additionalClauses = new HashMap<>();

    public AcquirerClausesDto() {
    }

    public Object getPaymentTerms() {
        return paymentTerms;
    }

    public void setPaymentTerms(Object paymentTerms) {
        this.paymentTerms = paymentTerms;
    }

    public Object getDelivery() {
        return delivery;
    }

    public void setDelivery(Object delivery) {
        this.delivery = delivery;
    }
    

    public Object getWarranty() {
        return warranty;
    }

    public void setWarranty(Object warranty) {
        this.warranty = warranty;
    }

    public Object getConfidentiality() {
        return confidentiality;
    }

    public void setConfidentiality(Object confidentiality) {
        this.confidentiality = confidentiality;
    }

    public Object getIntellectualProperty() {
        return intellectualProperty;
    }

    public void setIntellectualProperty(Object intellectualProperty) {
        this.intellectualProperty = intellectualProperty;
    }

    public Object getLiability() {
        return liability;
    }

    public void setLiability(Object liability) {
        this.liability = liability;
    }

    public Object getIndemnification() {
        return indemnification;
    }

    public void setIndemnification(Object indemnification) {
        this.indemnification = indemnification;
    }

    public Object getLatePaymentPenalty() {
        return latePaymentPenalty;
    }

    public void setLatePaymentPenalty(Object latePaymentPenalty) {
        this.latePaymentPenalty = latePaymentPenalty;
    }

    public Object getTermination() {
        return termination;
    }

    public void setTermination(Object termination) {
        this.termination = termination;
    }

    public Object getGoverningLaw() {
        return governingLaw;
    }

    public void setGoverningLaw(Object governingLaw) {
        this.governingLaw = governingLaw;
    }

    public Object getForceMajeure() {
        return forceMajeure;
    }

    public void setForceMajeure(Object forceMajeure) {
        this.forceMajeure = forceMajeure;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalClauses() {
        return additionalClauses;
    }

    @JsonAnySetter
    public void setAdditionalClause(String name, Object value) {
        this.additionalClauses.put(name, value);
    }
}
