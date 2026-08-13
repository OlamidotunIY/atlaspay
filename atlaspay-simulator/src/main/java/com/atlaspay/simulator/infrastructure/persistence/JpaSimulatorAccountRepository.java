package com.atlaspay.simulator.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaSimulatorAccountRepository extends JpaRepository<SimulatorAccountJpaEntity, String> {
}
