package com.hellostudy.modules.account.repository;

import com.hellostudy.modules.account.Account;
import com.hellostudy.modules.tag.Tag;
import com.hellostudy.modules.zone.Zone;

import java.util.List;
import java.util.Set;

public interface AccountRepositoryCustom {

    List<Account> findByTagsAndZone(Set<Tag> tags, Set<Zone> zones);
}
