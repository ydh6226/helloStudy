package com.hellostudy.modules.study.repository;

import com.hellostudy.modules.account.QAccount;
import com.hellostudy.modules.study.Study;
import com.hellostudy.modules.tag.QTag;
import com.hellostudy.modules.zone.QZone;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import javax.persistence.EntityManager;
import java.util.List;

import static com.hellostudy.modules.account.QAccount.account;
import static com.hellostudy.modules.study.QStudy.study;
import static com.hellostudy.modules.tag.QTag.tag;
import static com.hellostudy.modules.zone.QZone.zone;

public class StudyRepositoryImpl implements StudyRepositoryCustom {

    private final JPAQueryFactory query;

    public StudyRepositoryImpl(EntityManager em) {
        query = new JPAQueryFactory(em);
    }

    @Override
    public List<Study> findForSearchStudyByKeyword(String keyword) {
        return query.selectFrom(study)
                .leftJoin(study.managers, account).fetchJoin()
                .leftJoin(study.members, account).fetchJoin()
                .leftJoin(study.tags, tag).fetchJoin()
                .leftJoin(study.zones, zone).fetchJoin()
                .where((study.published.isTrue().and(study.closed.isFalse()))
                        .and(study.title.contains(keyword)
                                .or(tagTitleLikes(keyword))
                                .or(zoneLocalNameOfCityLikes(keyword))))
                .distinct()
                .fetch();
    }

    private BooleanExpression tagTitleLikes(String keyword) {
        return study.tags.any().title.contains(keyword);
    }

    private BooleanExpression zoneLocalNameOfCityLikes(String keyword) {
        return study.zones.any().localNameOfCity.contains(keyword);
    }
}
