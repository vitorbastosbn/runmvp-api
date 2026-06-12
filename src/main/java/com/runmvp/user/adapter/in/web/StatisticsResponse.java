package com.runmvp.user.adapter.in.web;

import com.runmvp.user.application.port.out.UserStatisticsRepository.Statistics;

public record StatisticsResponse(
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
) {
    public static StatisticsResponse from(Statistics s) {
        return new StatisticsResponse(
            s.userId(), s.totalSessions(), s.completedSessions(),
            s.officialDistanceMeters(), s.totalRunningTimeSeconds(),
            s.averagePaceSecPerKm(), s.longestDistanceMeters(),
            s.podiums(), s.firstPlaces(), s.completedCooperativeSessions()
        );
    }
}
