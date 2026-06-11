package com.runmvp.user.application.port.out;

import com.runmvp.user.domain.User;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByGoogleSubject(String googleSubject);
    Optional<User> findByPublicCode(String publicCode);
    boolean existsByPublicCode(String publicCode);
}
