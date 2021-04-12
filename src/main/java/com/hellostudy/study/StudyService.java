package com.hellostudy.study;

import com.hellostudy.domain.Account;
import com.hellostudy.domain.Study;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository studyRepository;

    @Transactional
    public Study createNewStudy(Study study, Account account) {
        Study newStudy = studyRepository.save(study);
        newStudy.addManager(account);
        return newStudy;
    }

    @Transactional
    public void updateDescription(String path, String shortDescription, String fullDescription) {
        Study study = getStudy(path);
        study.updateDescription(shortDescription, fullDescription);
    }

    public Study getStudyToUpdate(Account account, String path) {
        Study study = studyRepository.findStudyWithAllInfoByPath(path);
        if (!account.isManagerOf(study)) {
            throw new AccessDeniedException("해당 기능은 관리자만 이용 가능합니다.");
        }
        return study;
    }

    public Study getStudy(String path) {
        Study findStudy = studyRepository.findStudyWithAllInfoByPath(path);
        if (findStudy == null) {
            throw new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다.");
        }
        return findStudy;
    }
}
