package com.hellostudy.event;

import com.hellostudy.domain.Event;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.persistence.Entity;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    
    @EntityGraph(attributePaths = {"study", "createBy", "enrollments"})
    Optional<Event> findEventWithAllInfoById(Long id);
}
