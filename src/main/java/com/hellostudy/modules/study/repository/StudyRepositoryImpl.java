package com.hellostudy.modules.study.repository;

import com.hellostudy.modules.study.Study;
import com.hellostudy.modules.tag.Tag;
import com.hellostudy.modules.zone.Zone;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Set;

import static com.hellostudy.modules.study.QStudy.study;

public class StudyRepositoryImpl extends QuerydslRepositorySupport implements StudyRepositoryCustom {

    private final JPAQueryFactory query;

    public StudyRepositoryImpl(EntityManager em) {
        super(Study.class);
        query = new JPAQueryFactory(em);
    }

    @Override
    public PageImpl<Study> findPagedStudyByKeyword(String keyword, Pageable pageable) {
        JPAQuery<Study> queryResult = query.selectFrom(study)
                .where((study.published.isTrue().and(study.closed.isFalse()))
                        .and(studyTitleLikes(keyword)
                                .or(tagTitleLikes(keyword))
                                .or(zoneLocalNameOfCityLikes(keyword))));

        QueryResults<Study> pagingResult = getQuerydsl()
                .applyPagination(pageable, queryResult)
                .fetchResults();

        return new PageImpl<>(pagingResult.getResults(), pageable, pagingResult.getTotal());
    }

    @Override
    public List<Study> findStudyByTagsAndZones(Set<Tag> tags, Set<Zone> zones) {
        return query.selectFrom(study)
                .where(study.tags.any().in(tags)
                        .and(study.zones.any().in(zones)))
                .fetch();
    }
    private BooleanExpression studyTitleLikes(String keyword) {
        return study.title.containsIgnoreCase(keyword);
    }

    private BooleanExpression tagTitleLikes(String keyword) {
        return study.tags.any().title.containsIgnoreCase(keyword);
    }

    private BooleanExpression zoneLocalNameOfCityLikes(String keyword) {
        return study.zones.any().localNameOfCity.containsIgnoreCase(keyword);
    }
}
