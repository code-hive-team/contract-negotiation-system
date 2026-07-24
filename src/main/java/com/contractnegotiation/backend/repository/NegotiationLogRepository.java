package com.contractnegotiation.backend.repository;

import com.contractnegotiation.backend.entity.NegotiationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NegotiationLogRepository extends JpaRepository<NegotiationLog, Long> {

    List<NegotiationLog> findByNegotiationId(Long negotiationId);
}
