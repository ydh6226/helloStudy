package com.hellostudy.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@EqualsAndHashCode(of = "id")
@Getter
@Builder @NoArgsConstructor @AllArgsConstructor
public class Account {

    @Id @GeneratedValue
    @Column(name = "account_id")
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String nickname;

    private String password;

    private boolean emailVerified;

    private String emailCheckToken;

    private LocalDateTime emailCheckTokenGeneratedAt;

    private LocalDateTime joinedAt;

    private String bio;

    private String url;

    private String occupation;

    private String location;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String profileImage;

    private boolean studyCreatedByEmail;

    private boolean studyCreatedByWeb;

    private boolean studyEnrollmentResultByEmail;

    private boolean studyEnrollmentResultByWeb;

    private boolean studyUpdatedByEmail;

    private boolean studyUpdatedByWeb;

    public void generateEmailCheckToken() {
        this.emailCheckToken = UUID.randomUUID().toString();
    }

    public void completeSignUp() {
        emailVerified = true;
        joinedAt = LocalDateTime.now();
    }

    public boolean isValidToken(String token) {
        return this.emailCheckToken.equals(token);
    }

    public void initEmailTokenTime() {
        emailCheckTokenGeneratedAt = LocalDateTime.now();
    }

    public boolean canSendEmailToken() {
        if (emailCheckTokenGeneratedAt == null)
            return true;
        return emailCheckTokenGeneratedAt.isBefore(LocalDateTime.now().minusHours(1));
    }

    public void updateProfile(String bio, String url, String occupation, String location, String profileImage) {
        this.bio = bio;
        this.url = url;
        this.occupation = occupation;
        this.location = location;
        this.profileImage = profileImage;
        // TODO: 2021-03-30 이미지 수정 기능 추가
    }
}
