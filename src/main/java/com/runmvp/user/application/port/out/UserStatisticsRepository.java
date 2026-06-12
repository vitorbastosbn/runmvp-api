package com.runmvp.user.application.port.out;

import java.util.Optional;

public interface UserStatisticsRepository {
    record Statistics(
        Long userId,
        int totalSessions,
        int completedSessions,
        long officialDistanceMeters,
        long totalRunningTimeSeconds,
        Integer averagePaceSecPerKm,
        int longestDistanceMeters,
        int podiums,
        int firstPlaces,
        int completedCooperativeSessions
    ) {}

    Optional<Statistics> findByUserId(Long userId);
    void initializeForUser(Long userId);
}
