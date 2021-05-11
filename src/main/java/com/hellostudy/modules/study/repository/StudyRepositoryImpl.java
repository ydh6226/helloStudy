package com.hellostudy.modules.study.repository;

import com.hellostudy.modules.study.Study;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Objects;

import static com.hellostudy.modules.account.QAccount.account;
import static com.hellostudy.modules.study.QStudy.study;
import static com.hellostudy.modules.tag.QTag.tag;
import static com.hellostudy.modules.zone.QZone.zone;

public class StudyRepositoryImpl extends QuerydslRepositorySupport implements StudyRepositoryCustom {

    private final JPAQueryFactory query;

    public StudyRepositoryImpl(EntityManager em) {
        super(Study.class);
        query = new JPAQueryFactory(em);
    }

    @Override
    public PageImpl<Study> findForSearchStudyByKeyword(String keyword, Pageable pageable) {
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
