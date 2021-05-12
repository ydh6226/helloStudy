package com.hellostudy.modules.event.repository;

import com.hellostudy.modules.event.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long>, EnrollmentRepositoryCustom {

    @Query("select en from Enrollment en " +
            "join fetch en.event e " +
            "join fetch e.study")
    Enrollment findEnrollmentWithAccountByIdQuery(Long id);
}
