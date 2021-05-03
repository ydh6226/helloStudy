package com.hellostudy.modules.study;

import com.hellostudy.modules.account.Account;
import com.hellostudy.modules.study.event.StudyCreateEvent;
import com.hellostudy.modules.tag.Tag;
import com.hellostudy.modules.zone.Zone;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository studyRepository;
    private final ApplicationEventPublisher eventPublisher;

    public Study createNewStudy(Study study, Account account) {
        Study newStudy = studyRepository.save(study);
        newStudy.addManager(account);
        eventPublisher.publishEvent(new StudyCreateEvent(newStudy));
        return newStudy;
    }

    @Transactional(readOnly = true)
    public Study getStudyToUpdate(Account account, String path) {
        return studyVerification(studyRepository.findByPath(path), account, path);
    }

    @Transactional(readOnly = true)
    public Study getStudyWithManagers(Account account, String path) {
        return studyVerification(studyRepository.findStudyWithManagerByPath(path), account, path);
    }

    @Transactional(readOnly = true)
    public Study getStudyToUpdateDescription(Account account, String path) {
        return studyVerification(studyRepository.findStudyWithAllInfoByPath(path), account, path);
    }

    @Transactional(readOnly = true)
    public Study getStudyWithoutFetch(Account account, String path) {
        return studyVerification(studyRepository.findStudyWithoutFetchByPath(path), account, path);
    }

    @Transactional(readOnly = true)
    public Study getStudyForView(String path) {
        Study study = studyRepository.findStudyWithManagerByPath(path);
        isExistingStudy(study, path);
        return study;
    }

    @Transactional(readOnly = true)
    public Study getStudy(String path) {
        Study study = studyRepository.findByPath(path);
        isExistingStudy(study, path);
        return study;
    }

    @Transactional(readOnly = true)
    public Study getStudyWithMember(String path) {
        Study study = studyRepository.findStudyWithMembersByPath(path);
        isExistingStudy(study, path);
        return study;
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

    public void publish(Study study) {
        study.publish();
    }

    public void close(Study study) {
        study.close();
    }

    public void startRecruit(Study study) {
        study.startRecruit();
    }

    public void stopRecruit(Study study) {
        study.stopRecruit();
    }

    public Study pathVerification(String path) {
        Study study = studyRepository.findStudyWithoutFetchByPath(path);
        isExistingStudy(study, path);
        return study;
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

    public void updateTitle(Study study, String title) {
        study.updateTitle(title);
    }

    public void updatePath(Study study, String path) {
        study.updatePath(path);
    }

    @Transactional(readOnly = true)
    public boolean isValidPath(String newPath) {
        if (!newPath.matches("^[ㄱ-ㅎ가-힣a-z0-9_-]{2,20}$")){
            return false;
        }
        return !studyRepository.existsByPath(newPath);
    }

    public void deleteStudy(Study study) {
        if (!study.isRemovable()) {
            throw new RuntimeException("모집을 했던 스터디는 삭제할 수 없습니다.");
        }
        studyRepository.delete(study);
    }

    public void join(Account account, Study study) {
        study.addMember(account);
    }

    public void leave(Account account, Study study) {
        study.deleteMember(account);
    }
}
