package com.hellostudy.modules.study.repository;

import com.hellostudy.modules.study.Study;

import java.util.List;

public interface StudyRepositoryCustom {

    List<Study> findForSearchStudyByKeyword(String keyword);
}
