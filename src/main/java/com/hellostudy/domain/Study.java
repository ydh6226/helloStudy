package com.hellostudy.domain;

import com.hellostudy.account.UserAccount;
import lombok.*;

import javax.persistence.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Study {

    @Id @GeneratedValue
    private Long id;

    @ManyToMany
    private Set<Account> managers = new HashSet<>();
    
    @ManyToMany
    private Set<Account> members = new HashSet<>();

    @Column(unique = true)
    private String path;

    private String title;

    private String shortDescription;

    @Lob
    private String fullDescription;

    // TODO: 2021-04-08 이미지 파일명만 저장하기
    @Lob
    private String image;

    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    private Set<Zone> zones = new HashSet<>();

    private LocalDateTime publishedDateTime;

    private LocalDateTime closedDateTime;

    private LocalDateTime recruitingUpdatedDateTime;

    private boolean recruiting;

    private boolean published;

    private boolean closed;

    private boolean useBanner;

    public Study(String path, String title, String shortDescription, String fullDescription) {
        this.path = path;
        this.title = title;
        this.shortDescription = shortDescription;
        this.fullDescription = fullDescription;
    }

    public void addManager(Account account) {
        managers.add(account);
    }

    public void updateDescription(String shortDescription, String fullDescription) {
        this.shortDescription = shortDescription;
        this.fullDescription = fullDescription;
    }

    public boolean isMember(UserAccount userAccount) {
        return members.contains(userAccount.getAccount());
    }

    public boolean isManager(UserAccount userAccount) {
        return managers.contains(userAccount.getAccount());
    }

    public boolean isJoinable(UserAccount userAccount) {
        return this.isPublished() && this.isRecruiting()
                && !isManager(userAccount) && !isMember(userAccount);
    }

    public String getImage() {
        return image != null? image: "/images/default_banner.png";
    }

    public void EnableStudyBanner() {
        useBanner = true;
    }

    public void DisableStudyBanner() {
        useBanner = false;
    }

    public void updateBanner(String image) {
        this.image = image;
    }

    public void addTag(Tag tag) {
        tags.add(tag);
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
    }

    public void addZone(Zone zone) {
        zones.add(zone);
    }

    public void removeZone(Zone zone) {
        zones.remove(zone);
    }

    public void publish() {
        published = true;
        publishedDateTime = LocalDateTime.now();
    }

    public void close() {
        closed = true;
        closedDateTime = LocalDateTime.now();
    }

    public boolean canUpdateRecruitingStatus() {
        if (recruitingUpdatedDateTime == null) {
            return true;
        }
        return recruitingUpdatedDateTime.isBefore(LocalDateTime.now().minusHours(3));
    }

    public void startRecruit() {
        recruiting = true;
        recruitingUpdatedDateTime = LocalDateTime.now();
    }

    public void stopRecruit() {
        recruiting = false;
        recruitingUpdatedDateTime = LocalDateTime.now();
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updatePath(String path) {
        this.path = path;
    }

    public boolean isRemovable() {
        return !published;
    }

    public void addMember(Account account) {
        if (!members.add(account)) {
            throw new IllegalStateException("이미 가입한 스터디 입니다.");
        }
    }

    public void deleteMember(Account account) {
        if (!members.remove(account)) {
            throw new IllegalStateException("가입하지 않은 스터디 입니다.");
        }
    }

    public String getEncodePath() {
        return URLEncoder.encode(path, StandardCharsets.UTF_8);
    }
}
