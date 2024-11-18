package server.poptato.emoji.domain.repository;

import server.poptato.emoji.domain.entity.Emoji;

import java.util.Optional;

public interface EmojiRepository {
    Optional<Emoji> findById(Long id);
    String findImageUrlById(Long emojiId);
}
