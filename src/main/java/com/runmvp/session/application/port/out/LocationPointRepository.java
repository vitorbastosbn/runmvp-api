package com.runmvp.session.application.port.out;

import com.runmvp.session.domain.LocationPoint;
import java.util.List;
import java.util.Set;

public interface LocationPointRepository {
    void saveAll(List<LocationPoint> points);
    Set<Integer> findExistingSequences(Long participantId, Set<Integer> sequences);
}
