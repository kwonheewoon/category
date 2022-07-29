package report.category.repository;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import report.category.dto.CategoryDto;
import report.category.entity.QCategoryEntity;
import report.category.vo.CategoryVo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CategoryQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Page<CategoryDto> categoryList(CategoryVo vo, Pageable pageable){
        var categoryEntity = QCategoryEntity.categoryEntity;

        var content = this.queryFactory
                .select(Projections.fields(
                        CategoryDto.class,
                        categoryEntity.id,
                        categoryEntity.categoryNm,
                        categoryEntity.orderNo,
                        categoryEntity.deleteFlag,
                        categoryEntity.createDate,
                        categoryEntity.lastModifiedDate
                ))
                .from(categoryEntity)
                .where(
                        categoryNm(vo.getCategoryNm()),
                        categoryEntity.deleteFlag.eq("N")
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(content, pageable, content.size());
    }

    public Long deleteCategory(Long id) {
        var categoryEntity = QCategoryEntity.categoryEntity;
        var result = this.queryFactory
                .update(categoryEntity)
                .set(categoryEntity.deleteFlag, "Y")
                .set(categoryEntity.lastModifiedDate, LocalDateTime.now())
                .where(categoryEntity.id.eq(id))
                .execute();
        return result;
    }

    private BooleanExpression categoryNm(String categoryNm) {
        var categoryEntity = QCategoryEntity.categoryEntity;
        return categoryNm != null && !categoryNm.isEmpty() ? categoryEntity.categoryNm.containsIgnoreCase(categoryNm) : null;
    }
}
