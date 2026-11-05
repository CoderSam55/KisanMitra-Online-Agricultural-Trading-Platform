package com.kisanmitra.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "users") @Getter @Setter @NoArgsConstructor
public class User {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(nullable=false) private String name;
    @Column(nullable=false, unique=true) private String email;
    private String phone;
    @Column(nullable=false) private String password;
    private String userType = "farmer";
    private String state; private String district; private String taluka; private String village;
    @Column(length=500) private String bio;
    private String avatar;
    private double rating; private int ratingsCount; private int transactionsCount;
    private boolean verifiedPhone; private boolean verifiedIdentity;
    private String role = "user"; private boolean suspended;
    private String preferredLanguage = "en";
    private Instant createdAt = Instant.now(); private Instant updatedAt = Instant.now();
    @PreUpdate void touch() { updatedAt = Instant.now(); }
}
