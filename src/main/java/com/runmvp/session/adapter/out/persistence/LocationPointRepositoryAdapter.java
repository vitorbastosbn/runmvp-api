package com.runmvp.session.adapter.out.persistence;

import com.runmvp.session.application.port.out.LocationPointRepository;
import com.runmvp.session.domain.LocationPoint;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
class LocationPointRepositoryAdapter implements LocationPointRepository {

    private final LocationPointJpaRepository jpa;

    LocationPointRepositoryAdapter(LocationPointJpaRepository jpa) { this.jpa = jpa; }

    @Override
    public void saveAll(List<LocationPoint> points) {
        jpa.saveAll(points.stream().map(LocationPointJpaEntity::fromDomain).toList());
    }

    @Override
    public Set<Integer> findExistingSequences(Long participantId, Set<Integer> sequences) {
        return jpa.findExistingSequences(participantId, sequences);
    }
}
