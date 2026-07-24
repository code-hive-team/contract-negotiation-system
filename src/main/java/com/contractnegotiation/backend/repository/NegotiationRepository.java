package com.contractnegotiation.backend.repository;

import com.contractnegotiation.backend.entity.Negotiation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NegotiationRepository extends JpaRepository<Negotiation, Long> {

    List<Negotiation> findByIssuerContractIdOrAcquirerContractId(Long issuerContractId, Long acquirerContractId);
}
