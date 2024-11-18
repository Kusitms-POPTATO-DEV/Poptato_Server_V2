package server.poptato.category.application;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import server.poptato.category.api.request.CategoryCreateUpdateRequestDto;
import server.poptato.category.application.response.CategoryCreateResponseDto;
import server.poptato.category.application.response.CategoryListResponseDto;
import server.poptato.category.domain.repository.CategoryRepository;
import server.poptato.emoji.domain.repository.EmojiRepository;
import server.poptato.emoji.exception.EmojiException;
import server.poptato.emoji.validator.EmojiValidator;
import server.poptato.user.validator.UserValidator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static server.poptato.emoji.exception.errorcode.EmojiExceptionErrorCode.EMOJI_NOT_EXIST;

@Transactional
@SpringBootTest
public class CategoryServiceTest {
    @Autowired
    CategoryService categoryService;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    EmojiRepository emojiRepository;
    @Autowired
    UserValidator userValidator;
    @Autowired
    EmojiValidator emojiValidator;

    @DisplayName("카테고리 생성 시 성공한다.")
    @Test
    void createCategory_Success() {
        //given
        Long userId = 1L;
        String name = "카테고리";
        Long emojiId = 3L;
        CategoryCreateUpdateRequestDto request = new CategoryCreateUpdateRequestDto(name, emojiId);

        //when
        CategoryCreateResponseDto response = categoryService.createCategory(userId, request);

        //then
        assertThat(response.getCategoryId()).isEqualTo(11L);
    }

    @DisplayName("카테고리 생성 시 존재하지 않는 이모지이면 예외가 발생한다.")
    @Test
    void createCategory_Emoji_Not_Exist_Exception() {
        //given
        Long userId = 1L;
        String name = "카테고리";
        Long emojiId = 100L;
        CategoryCreateUpdateRequestDto request = new CategoryCreateUpdateRequestDto(name, emojiId);

        //then
        assertThatThrownBy(() -> categoryService.createCategory(userId, request))
                .isInstanceOf(EmojiException.class)
                .hasMessage(EMOJI_NOT_EXIST.getMessage());
    }

    @DisplayName("카테고리 목록 조회 시 성공한다.")
    @Test
    void getCategories_Success() {
        //given
        Long userId = 1L;
        int page = 0;
        int size = 6;

        //when
        CategoryListResponseDto response = categoryService.getCategories(userId, page, size);

        //then
        assertThat(response.categories().size()).isEqualTo(6);
    }

}
