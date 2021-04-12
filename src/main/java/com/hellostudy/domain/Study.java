package com.hellostudy.domain;

import com.hellostudy.account.UserAccount;
import lombok.*;

import javax.persistence.*;
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
}
