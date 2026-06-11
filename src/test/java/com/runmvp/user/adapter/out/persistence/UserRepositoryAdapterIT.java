package com.runmvp.user.adapter.out.persistence;

import com.runmvp.BaseIntegrationTest;
import com.runmvp.user.domain.User;
import com.runmvp.user.application.port.out.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

class UserRepositoryAdapterIT extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void save_thenFindById_returnsUser() {
        User user = User.create("sub-001","João","joao@x.com",null,"ABCD1234");
        User saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        Optional<User> found = userRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("João");
    }

    @Test
    void findByGoogleSubject_returnsUser() {
        User user = User.create("sub-002","Ana","ana@x.com","https://av.io/img.jpg","EFGH5678");
        userRepository.save(user);

        Optional<User> found = userRepository.findByGoogleSubject("sub-002");
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("ana@x.com");
    }

    @Test
    void findByPublicCode_returnsUser() {
        User user = User.create("sub-003","Carlos","c@x.com",null,"ZXCV9876");
        userRepository.save(user);

        Optional<User> found = userRepository.findByPublicCode("ZXCV9876");
        assertThat(found).isPresent();
        assertThat(found.get().getGoogleSubject()).isEqualTo("sub-003");
    }

    @Test
    void existsByPublicCode_true_whenExists() {
        User user = User.create("sub-004","Bia","b@x.com",null,"QWER1234");
        userRepository.save(user);
        assertThat(userRepository.existsByPublicCode("QWER1234")).isTrue();
    }

    @Test
    void existsByPublicCode_false_whenNotExists() {
        assertThat(userRepository.existsByPublicCode("NOTEXIST")).isFalse();
    }
}
