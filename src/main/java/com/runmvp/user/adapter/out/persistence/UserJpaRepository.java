package com.runmvp.user.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {
    Optional<UserJpaEntity> findByGoogleSubject(String googleSubject);
    Optional<UserJpaEntity> findByPublicCode(String publicCode);
    boolean existsByPublicCode(String publicCode);
}
