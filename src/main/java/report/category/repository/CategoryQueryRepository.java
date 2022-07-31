package report.category.repository;

import com.querydsl.core.dml.UpdateClause;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import report.category.dto.CategoryApiDto;
import report.category.dto.CategoryDto;
import report.category.entity.CategoryEntity;
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

    public Long maxOrderNo(Long parentCategoryId, int depth){
        var categoryEntity = QCategoryEntity.categoryEntity;
        return this.queryFactory.select(
                    //categoryEntity.orderNo.max().coalesce(0).as("max")
                     categoryEntity.orderNo.count().as("count")
                )
                .from(categoryEntity)
                .where(
                        parentCategoryId(parentCategoryId, depth),
                        categoryEntity.deleteFlag.eq("N"),
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
                        categoryEntity.depth,
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

    public CategoryApiDto findCategoryOne(Long searchCategoryId){
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
                        categoryEntity.id.eq(searchCategoryId),
                        categoryEntity.deleteFlag.eq("N")
                )
                .orderBy(categoryEntity.orderNo.asc())
                .fetchOne();
    }

    public List<CategoryApiDto> findAllChcildCategorys(List<Long> parentCategoryIds){
        var chcildCategoryEntity = QCategoryEntity.categoryEntity;
        var parentCategoryEntity = new QCategoryEntity("parentCategoryEntity");

        return this.queryFactory
                .select(Projections.fields(
                        CategoryApiDto.class,
                        chcildCategoryEntity.id,
                        chcildCategoryEntity.categoryNm,
                        chcildCategoryEntity.depth,
                        chcildCategoryEntity.orderNo,
                        chcildCategoryEntity.deleteFlag,
                        chcildCategoryEntity.createDate,
                        chcildCategoryEntity.lastModifiedDate,
                        Projections.constructor(
                                CategoryApiDto.ParentCategoryApiDto.class,
                                parentCategoryEntity.id.as("id"),
                                parentCategoryEntity.categoryNm.as("categoryNm")
                        ).as("parentCategory")

                ))
                .from(chcildCategoryEntity)
                .leftJoin(parentCategoryEntity)
                .on(chcildCategoryEntity.parentCategory.id.eq(parentCategoryEntity.id))
                .where(
                        chcildCategoryEntity.parentCategory.id.in(parentCategoryIds),
                        chcildCategoryEntity.deleteFlag.eq("N")
                )
                .orderBy(chcildCategoryEntity.orderNo.asc())
                .fetch();
    }

    public Long updateCategory(Long id, CategoryDto dto) {
        var categoryEntity = QCategoryEntity.categoryEntity;

        UpdateClause<JPAUpdateClause> updateBuilder = this.queryFactory.update(categoryEntity);

        //카테고리 명 수정
        if (!dto.getCategoryNm().isEmpty()) {
            updateBuilder.set(categoryEntity.categoryNm, dto.getCategoryNm());
        }
        //부모 카테고리 수정
        if (null != dto.getParentCategory()) {
            updateBuilder.set(categoryEntity.parentCategory, CategoryEntity.entityConvert(dto.getParentCategory()));
        }
        //카테고리 정렬번호 수정
        if (dto.getOrderNo() > 0) {
            updateBuilder.set(categoryEntity.orderNo, dto.getOrderNo());
        }
        //카테고리 깊이 수정
        if (dto.getDepth() > 0) {
            updateBuilder.set(categoryEntity.depth, dto.getDepth());
        }

        return updateBuilder
                .set(categoryEntity.lastModifiedDate, LocalDateTime.now())
                .where(categoryEntity.id.eq(id))
                .execute();
    }

    public Long updateCategoryOrderNo(Long id, int orderNo) {
        var categoryEntity = QCategoryEntity.categoryEntity;
        var result = this.queryFactory
                .update(categoryEntity)
                .set(categoryEntity.orderNo, orderNo)
                .set(categoryEntity.lastModifiedDate, LocalDateTime.now())
                .where(categoryEntity.id.eq(id))
                .execute();
        return result;
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
    private BooleanExpression parentCategoryId(Long parentCategoryId, int depth) {
        var categoryEntity = QCategoryEntity.categoryEntity;
        return depth > 1 ? categoryEntity.parentCategory.id.eq(parentCategoryId) : null;
    }
}
