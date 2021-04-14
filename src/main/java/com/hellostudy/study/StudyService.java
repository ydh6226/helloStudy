package com.hellostudy.study;

import com.hellostudy.domain.Account;
import com.hellostudy.domain.Study;
import com.hellostudy.domain.Tag;
import com.hellostudy.domain.Zone;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository studyRepository;

    public Study createNewStudy(Study study, Account account) {
        Study newStudy = studyRepository.save(study);
        newStudy.addManager(account);
        return newStudy;
    }

    @Transactional(readOnly = true)
    public Study getStudyToUpdate(Account account, String path) {
        return studyVerification(studyRepository.findByPath(path), account, path);
    }

    @Transactional(readOnly = true)
    public Study getStudyToUpdateDescription(Account account, String path) {
        return studyVerification(studyRepository.findStudyWithAllInfoByPath(path), account, path);
    }

    @Transactional(readOnly = true)
    public Study getStudy(String path) {
        return studyRepository.findByPath(path);
    }

    public void updateDescription(String path, String shortDescription, String fullDescription) {
        Study study = studyRepository.findStudyWithDescriptionByPath(path);
        study.updateDescription(shortDescription, fullDescription);
    }

    public void EnableStudyBanner(Study study) {
        study.EnableStudyBanner();
    }

    public void DisableStudyBanner(Study study) {
        study.DisableStudyBanner();
    }

    public void addTag(Account account, String path, Tag tag) {
        Study study = studyVerification(studyRepository.findStudyWithTagsAndManagersByPath(path), account, path);
        study.addTag(tag);
    }

    public void removeTag(Account account, String path, Tag tag) {
        Study study = studyVerification(studyRepository.findStudyWithTagsAndManagersByPath(path), account, path);
        study.removeTag(tag);
    }

    public void addZone(Account account, String path, Zone zone) {
        Study study = studyVerification(studyRepository.findByPath(path), account, path);
        study.addZone(zone);
    }

    public void removeZone(Account account, String path, Zone zone) {
        Study study = studyVerification(studyRepository.findByPath(path), account, path);
        study.removeZone(zone);
    }

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
        isExistingStudy(study, path);
        isManagerOfStudy(account, study);
        return study;
    }
}