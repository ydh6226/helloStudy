package com.hellostudy.modules.study.repository;

import com.hellostudy.modules.study.Study;
import com.hellostudy.modules.tag.Tag;
import com.hellostudy.modules.zone.Zone;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface StudyRepositoryCustom {

    PageImpl<Study> findPagedStudyByKeyword(String keyword, Pageable pageable);

    List<Study> findStudyByTagsAndZones(Set<Tag> tags, Set<Zone> zones);
}
