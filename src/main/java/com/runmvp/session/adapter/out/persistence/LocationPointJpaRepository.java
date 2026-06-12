package com.runmvp.session.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

interface LocationPointJpaRepository extends JpaRepository<LocationPointJpaEntity, Long> {

    @Query("SELECT p.sequence FROM LocationPointJpaEntity p " +
           "WHERE p.participantId = :participantId AND p.sequence IN :sequences")
    Set<Integer> findExistingSequences(Long participantId, Set<Integer> sequences);
}
