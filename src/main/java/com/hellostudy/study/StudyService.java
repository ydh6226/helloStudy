package com.hellostudy.study;

import com.hellostudy.domain.Account;
import com.hellostudy.domain.Study;
import com.hellostudy.domain.Tag;
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

    public Study getStudyToUpdate(Account account, String path) {
        return studyVerification(studyRepository.findByPath(path), account, path);
    }

    public Study getStudyToUpdateDescription(Account account, String path) {
        return studyVerification(studyRepository.findStudyWithAllInfoByPath(path), account, path);
    }

    public Study getStudyToUpdateTag(Account account, String path) {
        return studyVerification(studyRepository.findStudyWithTagsAndManagersByPath(path), account, path);
    }

    public Study getStudy(String path) {
        return studyRepository.findByPath(path);
    }

    @Transactional
    public void updateDescription(String path, String shortDescription, String fullDescription) {
        Study study = studyRepository.findStudyWithDescriptionByPath(path);
        study.updateDescription(shortDescription, fullDescription);
    }

    @Transactional
    public void EnableStudyBanner(Study study) {
        study.EnableStudyBanner();
    }

    @Transactional
    public void DisableStudyBanner(Study study) {
        study.DisableStudyBanner();
    }

    @Transactional
    public void addTag(Account account, String path, Tag tag) {
        Study study = studyVerification(studyRepository.findStudyWithTagsAndManagersByPath(path), account, path);
        study.addTag(tag);
    }

    @Transactional
    public void removeTag(Account account, String path, Tag tag) {
        Study study = studyVerification(studyRepository.findStudyWithTagsAndManagersByPath(path), account, path);
        study.removeTag(tag);
    }

    @Transactional
    public void updateBanner(Study study, String image) {
        study.updateBanner(image);
    }

    private void isManagerOfStudy(Account account, Study study) {
        if (!account.isManagerOf(study)) {
            throw new AccessDeniedException("해당 기능은 관리자만 이용 가능합니다.");
        }
    }

    private void isExistingStudy(Study study, String path) {
        if (study == null) {
            throw new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다.");
        }
    }

    private Study studyVerification(Study study, Account account, String path) {
        isManagerOfStudy(account, study);
        isExistingStudy(study, path);
        return study;
    }

}
