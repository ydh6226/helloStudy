package com.hellostudy.tag;

import com.hellostudy.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(readOnly = true)
public interface TagRepository extends JpaRepository<Tag, Long> {

    boolean existsByTitle(String title);

    Optional<Tag> findByTitle(String title);
}
