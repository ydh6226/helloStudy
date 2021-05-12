package com.hellostudy.modules.event.repository;

import com.hellostudy.modules.event.Enrollment;

import java.util.List;

public interface EnrollmentRepositoryCustom {

    List<Enrollment> findAllNeedToParticipateByAccountId(Long accountId);
}
