package com.runmvp.user.adapter.out.persistence;

import com.runmvp.user.domain.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users")
class UserJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "google_subject", nullable = false, unique = true)
    private String googleSubject;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "public_code", nullable = false, length = 12, unique = true)
    private String publicCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    static UserJpaEntity fromDomain(User u) {
        UserJpaEntity e = new UserJpaEntity();
        e.id = u.getId();
        e.googleSubject = u.getGoogleSubject();
        e.name = u.getName();
        e.email = u.getEmail();
        e.avatarUrl = u.getAvatarUrl();
        e.publicCode = u.getPublicCode();
        e.createdAt = u.getCreatedAt();
        e.deletedAt = u.getDeletedAt();
        return e;
    }

    User toDomain() {
        User u = User.create(googleSubject, name, email, avatarUrl, publicCode);
        u.setId(id);
        u.setCreatedAt(createdAt);
        u.setDeletedAt(deletedAt);
        return u;
    }
}
