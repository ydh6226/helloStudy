package com.hellostudy.modules.main;

import com.hellostudy.modules.study.Study;
import com.hellostudy.modules.study.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MainService {

    private final StudyRepository studyRepository;

    public List<Study> findByStudyByKeyword(String keyword) {
        return studyRepository.findForSearchStudyByKeyword(keyword);
    }
}
