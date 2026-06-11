package com.runmvp.user.domain;

import java.time.Instant;

public class User {

    private Long id;
    private String googleSubject;
    private String name;
    private String email;
    private String avatarUrl;
    private String publicCode;
    private Instant createdAt;
    private Instant deletedAt;

    private User() {}

    public static User create(String googleSubject, String name, String email,
                              String avatarUrl, String publicCode) {
        User u = new User();
        u.googleSubject = googleSubject;
        u.name = name;
        u.email = email;
        u.avatarUrl = avatarUrl;
        u.publicCode = publicCode;
        u.createdAt = Instant.now();
        return u;
    }

    public void updateProfile(String name, String avatarUrl) {
        this.name = name;
        this.avatarUrl = avatarUrl;
    }

    public void softDelete() {
        if (this.deletedAt == null) {
            this.deletedAt = Instant.now();
        }
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public Long getId()            { return id; }
    public void setId(Long id)     { this.id = id; }
    public String getGoogleSubject() { return googleSubject; }
    public String getName()        { return name; }
    public String getEmail()       { return email; }
    public String getAvatarUrl()   { return avatarUrl; }
    public String getPublicCode()  { return publicCode; }
    public Instant getCreatedAt()  { return createdAt; }
    public Instant getDeletedAt()  { return deletedAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
}
