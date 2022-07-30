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
import report.category.dto.CategoryApiDto;
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

    public int maxOrderNo(int depth){
        var categoryEntity = QCategoryEntity.categoryEntity;
        return this.queryFactory.select(
                categoryEntity.orderNo.max()
        ).from(categoryEntity)
                .where(categoryEntity.deleteFlag.eq("N"),
                        categoryEntity.depth.eq(depth))
                .fetchOne();
    }

    public List<CategoryApiDto> findAllcategorys(CategoryVo vo){
        var categoryEntity = QCategoryEntity.categoryEntity;
        var parentCategoryEntity = new QCategoryEntity("parentCategoryEntity");

        return this.queryFactory
                .select(Projections.fields(
                        CategoryApiDto.class,
                        categoryEntity.id,
                        categoryEntity.categoryNm,
                        categoryEntity.orderNo,
                        categoryEntity.deleteFlag,
                        categoryEntity.createDate,
                        categoryEntity.lastModifiedDate,
                        Projections.constructor(
                                CategoryApiDto.ParentCategoryApiDto.class,
                                parentCategoryEntity.id.as("id"),
                                parentCategoryEntity.categoryNm.as("categoryNm")
                        ).as("parentCategory")

                ))
                .from(categoryEntity)
                .leftJoin(parentCategoryEntity)
                .on(categoryEntity.parentCategory.id.eq(parentCategoryEntity.id))
                .where(
                        categoryEntity.depth.eq(1),
                        categoryEntity.deleteFlag.eq("N")
                )
                .orderBy(categoryEntity.orderNo.asc())
                .fetch();
    }

    public CategoryApiDto findCategoryOne(CategoryVo vo){
        var categoryEntity = QCategoryEntity.categoryEntity;
        var parentCategoryEntity = new QCategoryEntity("parentCategoryEntity");

        return this.queryFactory
                .select(Projections.fields(
                        CategoryApiDto.class,
                        categoryEntity.id,
                        categoryEntity.categoryNm,
                        categoryEntity.orderNo,
                        categoryEntity.deleteFlag,
                        categoryEntity.createDate,
                        categoryEntity.lastModifiedDate,
                        Projections.constructor(
                                CategoryApiDto.ParentCategoryApiDto.class,
                                parentCategoryEntity.id.as("id"),
                                parentCategoryEntity.categoryNm.as("categoryNm")
                        ).as("parentCategory")

                ))
                .from(categoryEntity)
                .leftJoin(parentCategoryEntity)
                .on(categoryEntity.parentCategory.id.eq(parentCategoryEntity.id))
                .where(
                        categoryEntity.id.eq(vo.getSearchCategoryId()),
                        categoryEntity.deleteFlag.eq("N")
                )
                .orderBy(categoryEntity.orderNo.asc())
                .fetchOne();
    }

    public List<CategoryApiDto> findAllChcildCategorys(List<Long> parentCategoryIds){
        var chcildCategoryEntity = QCategoryEntity.categoryEntity;

        return this.queryFactory
                .select(Projections.fields(
                        CategoryApiDto.class,
                        chcildCategoryEntity.id,
                        chcildCategoryEntity.categoryNm,
                        chcildCategoryEntity.orderNo,
                        chcildCategoryEntity.deleteFlag,
                        chcildCategoryEntity.createDate,
                        chcildCategoryEntity.lastModifiedDate

                ))
                .from(chcildCategoryEntity)
                .where(
                        chcildCategoryEntity.parentCategory.id.in(parentCategoryIds),
                        chcildCategoryEntity.deleteFlag.eq("N")
                )
                .fetch();
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
    private BooleanExpression parentCategoryId(Long parentCategoryId) {
        var categoryEntity = QCategoryEntity.categoryEntity;
        return parentCategoryId != null && 0 < parentCategoryId ? categoryEntity.id.eq(parentCategoryId) : null;
    }
}
