package com.hellostudy.modules.main;

import com.hellostudy.modules.study.Study;
import com.hellostudy.modules.study.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MainService {

    private final StudyRepository studyRepository;

    public PageImpl<Study> findPagedStudyByKeyword(String keyword, Pageable pageable) {
        return studyRepository.findPagedStudyByKeyword(keyword, pageable);
    }
}
