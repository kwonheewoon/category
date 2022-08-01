package report.category;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import report.category.config.QueryDslConfiguration;
import report.category.dto.CategoryApiDto;
import report.category.entity.CategoryEntity;
import report.category.entity.QCategoryEntity;
import report.category.repository.CategoryRepository;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@AutoConfigureTestDatabase
@Import(QueryDslConfiguration.class)
public class CategoryQueryRepositoryTest {

    @Autowired
    JPAQueryFactory queryFactory;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void 카테고리_저장(){
        var categoryEntity = CategoryEntity
                .builder()
                .categoryNm("카테고리명 1")
                .build();

        //카테고리 저장
        var result = categoryRepository.save(categoryEntity);

        //카테고리 저장후 entity null 여부 테스트
        assertNotNull(result);
        //저장된 카테고리 조회 null 여부 테스트
        Assertions.assertThat(categoryEntity.getCategoryNm()).isEqualTo(result.getCategoryNm());
    }

    @Test
    void 상위_카테고리_조회(){
        var categoryEntity = CategoryEntity
                .builder()
                .categoryNm("카테고리명 1")
                .build();

        //카테고리 저장
        var result = categoryRepository.save(categoryEntity);

        //카테고리 조회
        //var findCategory = categoryQueryRepository.findCategoryOne(result.getId());

        var QcategoryEntity = QCategoryEntity.categoryEntity;
        var QparentCategoryEntity = new QCategoryEntity("parentCategoryEntity");

        var findCategory = this.queryFactory
                .select(Projections.fields(
                        CategoryApiDto.class,
                        QcategoryEntity.id,
                        QcategoryEntity.categoryNm,
                        QcategoryEntity.orderNo,
                        QcategoryEntity.deleteFlag,
                        QcategoryEntity.createDate,
                        QcategoryEntity.lastModifiedDate,
                        Projections.constructor(
                                CategoryApiDto.ParentCategoryApiDto.class,
                                QparentCategoryEntity.id.as("id"),
                                QparentCategoryEntity.categoryNm.as("categoryNm")
                        ).as("parentCategory")

                ))
                .from(QcategoryEntity)
                .leftJoin(QparentCategoryEntity)
                .on(QcategoryEntity.parentCategory.id.eq(QparentCategoryEntity.id))
                .where(
                        QcategoryEntity.id.eq(result.getId()),
                        QcategoryEntity.deleteFlag.eq("N")
                )
                .orderBy(QcategoryEntity.orderNo.asc())
                .fetchOne();


        //카테고리 저장후 entity null 여부 테스트
        assertNotNull(result);
        //저장된 카테고리 조회 null 여부 테스트
        assertNotNull(findCategory);
        Assertions.assertThat(categoryEntity.getCategoryNm()).isEqualTo(result.getCategoryNm());
        Assertions.assertThat(result.getCategoryNm()).isEqualTo(findCategory.getCategoryNm());
    }
}
