package com.hellostudy.tag;

import com.hellostudy.domain.Study;
import com.hellostudy.domain.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    @Transactional
    public Tag findOrCreate(String title) {
        return tagRepository.findByTitle(title)
                .orElseGet(() -> tagRepository.save(new Tag(title)));
    }
}
