package server.poptato.category.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.poptato.category.api.request.CategoryCreateUpdateRequestDto;
import server.poptato.category.application.response.CategoryCreateResponseDto;
import server.poptato.category.application.response.CategoryListResponseDto;
import server.poptato.category.application.response.CategoryResponseDto;
import server.poptato.category.domain.entity.Category;
import server.poptato.category.domain.repository.CategoryRepository;
import server.poptato.category.exception.CategoryException;
import server.poptato.category.validator.CategoryValidator;
import server.poptato.emoji.domain.repository.EmojiRepository;
import server.poptato.emoji.validator.EmojiValidator;
import server.poptato.user.validator.UserValidator;

import java.util.List;
import java.util.stream.Collectors;

import static server.poptato.category.exception.errorcode.CategoryExceptionErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final UserValidator userValidator;
    private final EmojiValidator emojiValidator;
    private final CategoryValidator categoryValidator;
    private final EmojiRepository emojiRepository;
    public CategoryCreateResponseDto createCategory(Long userId, CategoryCreateUpdateRequestDto request) {
        userValidator.checkIsExistUser(userId);
        emojiValidator.checkIsExistEmoji(request.emojiId());
        int maxCategoryId = categoryRepository.findMaxCategoryOrderByUserId(userId).orElseThrow(
                ()->new CategoryException(DEFAULT_CATEGORY_NOT_EXIST));
        Category newCategory = categoryRepository.save(Category.create(userId,maxCategoryId,request));
        return CategoryCreateResponseDto.builder().categoryId(newCategory.getId()).build();
    }

    public CategoryListResponseDto getCategories(Long userId, int page, int size) {
        userValidator.checkIsExistUser(userId);
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Category> categories = categoryRepository.findCategories(userId, pageRequest);
        return convertToCategoryListDto(categories);
    }

    private CategoryListResponseDto convertToCategoryListDto(Page<Category> categories) {
        List<CategoryResponseDto> categoryResponseDtoList = categories.stream()
                .map(category -> {
                    String imageUrl = emojiRepository.findImageUrlById(category.getEmojiId());
                    return new CategoryResponseDto(category, imageUrl);
                })
                .collect(Collectors.toList());

        return new CategoryListResponseDto(categoryResponseDtoList, categories.getTotalPages());
    }

    public void updateCategory(Long userId, Long categoryId, CategoryCreateUpdateRequestDto updateRequestDto) {
        userValidator.checkIsExistUser(userId);
        emojiValidator.checkIsExistEmoji(updateRequestDto.emojiId());
        Category category = categoryValidator.validateAndReturnCategory(userId, categoryId);
        category.update(updateRequestDto);
        categoryRepository.save(category);
    }

    public void deleteCategory(Long userId, Long categoryId) {
        userValidator.checkIsExistUser(userId);
        Category category = categoryValidator.validateAndReturnCategory(userId, categoryId);
        categoryRepository.delete(category);
    }
}
