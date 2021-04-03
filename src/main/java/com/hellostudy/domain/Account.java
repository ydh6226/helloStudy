package com.hellostudy.domain;

import com.hellostudy.settings.form.NotificationsForm;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@EqualsAndHashCode(of = "id")
@Getter
@Builder @Setter @NoArgsConstructor @AllArgsConstructor
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

    private String emailLoginToken;

    private LocalDateTime emailLoginTokenGeneratedAt;

    private String bio;

    private String url;

    private String occupation;

    private String location;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String profileImage;

    private boolean studyCreatedByEmail;

    private boolean studyCreatedByWeb = true;

    private boolean studyEnrollmentResultByEmail;

    private boolean studyEnrollmentResultByWeb = true;

    private boolean studyUpdatedByEmail;

    private boolean studyUpdatedByWeb = true;

    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

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

    public void generateEmailLoginToken() {
        this.emailLoginToken = UUID.randomUUID().toString();
        this.emailLoginTokenGeneratedAt = LocalDateTime.now();
    }


    public boolean canSendEmailLoginToken() {
        if (emailLoginTokenGeneratedAt == null)
            return true;
        return emailLoginTokenGeneratedAt.isBefore(LocalDateTime.now().minusHours(1));
    }


    public void updateProfile(String bio, String url, String occupation, String location, String profileImage) {
        this.bio = bio;
        this.url = url;
        this.occupation = occupation;
        this.location = location;
        this.profileImage = profileImage;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateNotifications(NotificationsForm form) {
        this.studyCreatedByEmail = form.isStudyCreatedByEmail();
        this.studyCreatedByWeb = form.isStudyCreatedByWeb();
        this.studyEnrollmentResultByEmail = form.isStudyEnrollmentResultByEmail();
        this.studyEnrollmentResultByWeb = form.isStudyEnrollmentResultByWeb();
        this.studyUpdatedByEmail = form.isStudyUpdatedByEmail();
        this.studyUpdatedByWeb = form.isStudyUpdatedByWeb();
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }


    public void addTag(Tag tag) {
        tags.add(tag);
    }

}
