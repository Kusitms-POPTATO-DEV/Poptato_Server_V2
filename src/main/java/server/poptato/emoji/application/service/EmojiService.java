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

    public EmojiResponseDTO getGroupedEmojis(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Emoji> emojiPage = emojiRepository.findAllEmojis(pageRequest);

        // groupName을 기준으로 그룹핑
        Map<String, List<EmojiDTO>> groupedEmojis = emojiPage.getContent().stream()
                .filter(emoji -> emoji.getGroupName() != null) // groupName이 null인 경우 제외
                .collect(Collectors.groupingBy(
                        emoji -> emoji.getGroupName().name(), // Enum의 name()을 키로 사용
                        Collectors.mapping(
                                emoji -> new EmojiDTO(emoji.getId(), emoji.getImageUrl()),
                                Collectors.toList()
                        )
                ));

        int totalPageCount = emojiPage.getTotalPages();

        return new EmojiResponseDTO(groupedEmojis, totalPageCount);
    }
}