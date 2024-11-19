package server.poptato.emoji.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import server.poptato.emoji.domain.entity.Emoji;

import java.util.Optional;

public interface EmojiRepository {
    Optional<Emoji> findById(Long id);
    String findImageUrlById(Long emojiId);
    Page<Emoji> findAll(PageRequest pageRequest);
}
