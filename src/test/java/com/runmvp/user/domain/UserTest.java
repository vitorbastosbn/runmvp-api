package com.runmvp.user.domain;

import org.junit.jupiter.api.Test;
import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class UserTest {

    @Test
    void create_setsAllFields() {
        User user = User.create(
            "google-sub-123",
            "Maria Silva",
            "maria@example.com",
            "https://avatar.example.com/maria.jpg",
            "ABCD1234"
        );

        assertThat(user.getId()).isNull();
        assertThat(user.getGoogleSubject()).isEqualTo("google-sub-123");
        assertThat(user.getName()).isEqualTo("Maria Silva");
        assertThat(user.getEmail()).isEqualTo("maria@example.com");
        assertThat(user.getAvatarUrl()).isEqualTo("https://avatar.example.com/maria.jpg");
        assertThat(user.getPublicCode()).isEqualTo("ABCD1234");
        assertThat(user.isDeleted()).isFalse();
        assertThat(user.getCreatedAt()).isNotNull();
    }

    @Test
    void create_withNullAvatar_isAllowed() {
        User user = User.create("sub", "Nome", "email@x.com", null, "CODE1234");
        assertThat(user.getAvatarUrl()).isNull();
    }

    @Test
    void updateProfile_changesNameAndAvatar() {
        User user = User.create("sub", "Old Name", "e@x.com", null, "CODE1234");
        user.updateProfile("New Name", "https://new-avatar.com/img.jpg");
        assertThat(user.getName()).isEqualTo("New Name");
        assertThat(user.getAvatarUrl()).isEqualTo("https://new-avatar.com/img.jpg");
    }

    @Test
    void softDelete_setsDeletedAt() {
        User user = User.create("sub", "Nome", "e@x.com", null, "CODE1234");
        assertThat(user.isDeleted()).isFalse();
        user.softDelete();
        assertThat(user.isDeleted()).isTrue();
        assertThat(user.getDeletedAt()).isNotNull();
    }

    @Test
    void softDelete_calledTwice_doesNotChangeDeletedAt() {
        User user = User.create("sub", "Nome", "e@x.com", null, "CODE1234");
        user.softDelete();
        Instant first = user.getDeletedAt();
        user.softDelete();
        assertThat(user.getDeletedAt()).isEqualTo(first);
    }
}
