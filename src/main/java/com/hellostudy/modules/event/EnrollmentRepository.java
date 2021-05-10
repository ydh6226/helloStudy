package com.hellostudy.modules.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    @Query("select en from Enrollment en " +
            "join fetch en.event e " +
            "join fetch e.study")
    Enrollment findEnrollmentWithAccountByIdQuery(Long id);
}
