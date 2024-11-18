package server.poptato.category.infra;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import server.poptato.category.domain.entity.Category;
import server.poptato.category.domain.repository.CategoryRepository;

import java.util.Optional;

public interface JpaCategoryRepository extends CategoryRepository, JpaRepository<Category, Long> {
    @Query("SELECT MAX(c.categoryOrder) FROM Category c WHERE c.userId = :userId")
    Optional<Integer> findMaxCategoryOrderByUserId(@Param("userId") Long userId);
}
