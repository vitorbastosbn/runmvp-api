package com.runmvp.user.adapter.out.persistence;

import com.runmvp.user.application.port.out.UserStatisticsRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
class UserStatisticsRepositoryAdapter implements UserStatisticsRepository {

    private final UserStatisticsJpaRepository jpa;

    UserStatisticsRepositoryAdapter(UserStatisticsJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Statistics> findByUserId(Long userId) {
        return jpa.findById(userId).map(UserStatisticsJpaEntity::toRecord);
    }

    @Override
    public void initializeForUser(Long userId) {
        if (!jpa.existsById(userId)) {
            jpa.save(UserStatisticsJpaEntity.empty(userId));
        }
    }
}
