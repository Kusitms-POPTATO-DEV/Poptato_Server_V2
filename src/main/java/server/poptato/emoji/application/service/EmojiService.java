package server.poptato.emoji.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import server.poptato.emoji.application.response.EmojiDTO;
import server.poptato.emoji.application.response.EmojiResponseDTO;
import server.poptato.emoji.domain.entity.Emoji;
import server.poptato.emoji.domain.repository.EmojiRepository;
import server.poptato.emoji.exception.EmojiException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static server.poptato.emoji.exception.errorcode.EmojiExceptionErrorCode.EMOJI_NOT_EXIST;

@Service
@RequiredArgsConstructor
public class EmojiService {

    private final EmojiRepository emojiRepository;

    private static final Map<String, String> GROUP_KEYWORD_MAP = Map.of(
            "생산성", "productive",
            "데일리", "daily",
            "취미", "hobby",
            "운동", "sports",
            "카테고리컬", "categorical"
    );

    public EmojiResponseDTO getGroupedEmojis(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Emoji> emojiPage = emojiRepository.findAll(pageRequest);

        Map<String, List<EmojiDTO>> groupedEmojis = emojiPage.getContent().stream()
                .collect(Collectors.groupingBy(
                        emoji -> findGroupByImageUrl(emoji.getImageUrl(), GROUP_KEYWORD_MAP),
                        Collectors.mapping(
                                emoji -> new EmojiDTO(emoji.getId(), emoji.getImageUrl()),
                                Collectors.toList()
                        )
                ));

        int totalPageCount = emojiPage.getTotalPages();

        return new EmojiResponseDTO(groupedEmojis, totalPageCount);
    }

    private String findGroupByImageUrl(String imageUrl, Map<String, String> groupKeywordMap) {
        return groupKeywordMap.entrySet().stream()
                .filter(entry -> imageUrl.contains(entry.getValue()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(()-> new EmojiException(EMOJI_NOT_EXIST));
    }
}