package com.hellostudy.event;

import com.hellostudy.domain.Event;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    
    @EntityGraph(attributePaths = {"study", "createBy", "enrollments"})
    Optional<Event> findEventWithAllInfoById(Long id);

    @EntityGraph(attributePaths = {"enrollments"})
    List<Event> findEventWithEnrollmentsByStudyId(Long studyId);
}
