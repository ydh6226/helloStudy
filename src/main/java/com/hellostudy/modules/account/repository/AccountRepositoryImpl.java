package com.hellostudy.modules.account.repository;

import com.hellostudy.modules.account.Account;
import com.hellostudy.modules.account.QAccount;
import com.hellostudy.modules.tag.Tag;
import com.hellostudy.modules.zone.Zone;
import com.querydsl.jpa.impl.JPAQueryFactory;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Set;

import static com.hellostudy.modules.account.QAccount.account;

public class AccountRepositoryImpl implements AccountRepositoryCustom{

    private final JPAQueryFactory query;

    public AccountRepositoryImpl(EntityManager em) {
        query = new JPAQueryFactory(em);
    }

    @Override
    public List<Account> findByTagsAndZone(Set<Tag> tags, Set<Zone> zones) {
        return query.select(account)
                .from(account)
                .where(account.tags.any().in(tags), account.zones.any().in(zones))
                .distinct()
                .fetch();
    }
}
