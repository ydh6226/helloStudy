package com.hellostudy.modules.study;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface StudyRepository extends JpaRepository<Study, Long> {
    boolean existsByPath(String path);

    Study findStudyWithoutFetchByPath(String path);

    @EntityGraph(attributePaths = {"managers"})
    Study findStudyWithManagerByPath(String path);

    @EntityGraph(attributePaths = {"members"})
    Study findStudyWithMembersByPath(String path);

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
