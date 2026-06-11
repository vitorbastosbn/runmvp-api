package com.runmvp.user.adapter.out.persistence;

import com.runmvp.user.application.port.out.UserRepository;
import com.runmvp.user.domain.User;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpa;

    UserRepositoryAdapter(UserJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public User save(User user) {
        return jpa.save(UserJpaEntity.fromDomain(user)).toDomain();
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpa.findById(id).map(UserJpaEntity::toDomain);
    }

    @Override
    public Optional<User> findByGoogleSubject(String googleSubject) {
        return jpa.findByGoogleSubject(googleSubject).map(UserJpaEntity::toDomain);
    }

    @Override
    public Optional<User> findByPublicCode(String publicCode) {
        return jpa.findByPublicCode(publicCode).map(UserJpaEntity::toDomain);
    }

    @Override
    public boolean existsByPublicCode(String publicCode) {
        return jpa.existsByPublicCode(publicCode);
    }
}
