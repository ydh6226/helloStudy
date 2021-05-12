package com.hellostudy.modules.event.repository;

import com.hellostudy.modules.event.Enrollment;
import com.hellostudy.modules.event.QEnrollment;
import com.hellostudy.modules.event.QEvent;
import com.hellostudy.modules.study.QStudy;
import com.querydsl.jpa.impl.JPAQueryFactory;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static com.hellostudy.modules.event.QEnrollment.enrollment;
import static com.hellostudy.modules.event.QEvent.event;
import static com.hellostudy.modules.study.QStudy.study;

public class EnrollmentRepositoryImpl implements EnrollmentRepositoryCustom {

    private final JPAQueryFactory query;

    public EnrollmentRepositoryImpl(EntityManager em) {
        query = new JPAQueryFactory(em);
    }

    @Override
    public List<Enrollment> findAllNeedToParticipateByAccountId(Long accountId) {
        return query.selectFrom(enrollment)
                .leftJoin(enrollment.event, event).fetchJoin()
                .leftJoin(event.study, study).fetchJoin()
                .where(enrollment.accepted.isTrue()
                        .and(enrollment.attended.isFalse()
                        .and(event.startDateTime.after(LocalDateTime.now()))
                        .and(enrollment.account.id.eq(accountId))))
                .fetch();
    }
}
