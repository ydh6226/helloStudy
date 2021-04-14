package com.hellostudy.study;

import com.hellostudy.domain.Study;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Entity;
import java.util.Optional;

@Transactional(readOnly = true)
public interface StudyRepository extends JpaRepository<Study, Long> {
    boolean existsByPath(String path);

    @EntityGraph(attributePaths = {"managers", "members", "tags", "zones"})
    Study findByPath(String path);

    @EntityGraph(attributePaths = {"managers", "members", "tags", "zones", "fullDescription"})
    Study findStudyWithAllInfoByPath(String path);

    @EntityGraph(attributePaths = {"fullDescription"})
    Study findStudyWithDescriptionByPath(String path);

    @EntityGraph(attributePaths = {"managers", "tags"})
    Study findStudyWithTagsAndManagersByPath(String path);

    @EntityGraph(attributePaths = {"managers", "zones"})
    Study findStudyWithZonesAndManagersByPath(String path);

    @EntityGraph(attributePaths = {"tags"})
    Study findStudyWithTagsByPath(String path);
}
