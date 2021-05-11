package com.hellostudy.modules.study.repository;

import com.hellostudy.modules.study.Study;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StudyRepositoryCustom {

    PageImpl<Study> findForSearchStudyByKeyword(String keyword, Pageable pageable);
}
