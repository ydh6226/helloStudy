package com.hellostudy.modules.event;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    
    @EntityGraph(attributePaths = {"study", "createBy", "enrollments"})
    Optional<Event> findEventWithAllInfoById(Long id);

    @EntityGraph(attributePaths = {"enrollments"})
    List<Event> findEventWithEnrollmentsByStudyId(Long studyId);

    Optional<Event> findEventWithoutFetchById(Long studyId);

    @EntityGraph(attributePaths = {"enrollments"})
    Optional<Event> findEventWithEnrollmentsById(Long id);

    @Query("select e from Event e " +
            "left join fetch e.enrollments enroll " +
            "left join fetch enroll.account " +
            "where e.id = :id")
    Optional<Event> findEventForJoinByIdQuery(@Param("id") Long id);
}
