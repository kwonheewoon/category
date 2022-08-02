package report.category.repository;

import com.querydsl.core.dml.UpdateClause;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import report.category.dto.CategoryApiDto;
import report.category.dto.CategoryDto;
import report.category.entity.CategoryEntity;
import report.category.entity.QCategoryEntity;
import report.category.vo.CategoryVo;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CategoryQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 부모 Category id 와 현 depth가 일치하는 Category 카운팅
    * */
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

    /**
     * 전체 Category 조회
    * */
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

    /**
     * 상위 Category 조회
    * */
    public Optional<CategoryApiDto> findCategoryOne(Long searchCategoryId){
        var categoryEntity = QCategoryEntity.categoryEntity;
        var parentCategoryEntity = new QCategoryEntity("parentCategoryEntity");

        return Optional.ofNullable(this.queryFactory
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
                        categoryEntity.id.eq(searchCategoryId),
                        categoryEntity.deleteFlag.eq("N")
                )
                .orderBy(categoryEntity.orderNo.asc())
                .fetchOne());
    }

    /**
     * 자식 Category 조회
    * */
    public List<CategoryApiDto> findAllChildCategorys(List<Long> parentCategoryIds){
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

    /**
     * Category 수정
    * */
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

    /**
     * Category orderNo 수정
    * */
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

    /**
     *
    * */
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
