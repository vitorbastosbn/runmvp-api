package com.runmvp.user.adapter.out.persistence;

import com.runmvp.user.application.port.out.UserStatisticsRepository;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_statistics")
class UserStatisticsJpaEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "total_sessions")          private int totalSessions;
    @Column(name = "completed_sessions")      private int completedSessions;
    @Column(name = "abandoned_sessions")      private int abandonedSessions;
    @Column(name = "official_distance_meters") private long officialDistanceMeters;
    @Column(name = "total_running_time_seconds") private long totalRunningTimeSeconds;
    @Column(name = "average_pace_sec_per_km") private Integer averagePaceSecPerKm;
    @Column(name = "longest_distance_meters") private int longestDistanceMeters;
    @Column(name = "podiums")                 private int podiums;
    @Column(name = "first_places")            private int firstPlaces;
    @Column(name = "completed_cooperative_sessions") private int completedCooperativeSessions;
    @Column(name = "updated_at")              private Instant updatedAt;
    @Version
    @Column(name = "version")                 private long version;

    static UserStatisticsJpaEntity empty(Long userId) {
        UserStatisticsJpaEntity e = new UserStatisticsJpaEntity();
        e.userId = userId;
        e.updatedAt = Instant.now();
        return e;
    }

    UserStatisticsRepository.Statistics toRecord() {
        return new UserStatisticsRepository.Statistics(
            userId, totalSessions, completedSessions, officialDistanceMeters,
            totalRunningTimeSeconds, averagePaceSecPerKm, longestDistanceMeters,
            podiums, firstPlaces, completedCooperativeSessions
        );
    }
}
