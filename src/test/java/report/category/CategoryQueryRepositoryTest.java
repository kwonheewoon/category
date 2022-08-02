package report.category;

import com.querydsl.core.dml.UpdateClause;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import report.category.config.QueryDslConfiguration;
import report.category.dto.CategoryApiDto;
import report.category.dto.CategoryDto;
import report.category.entity.CategoryEntity;
import report.category.entity.QCategoryEntity;
import report.category.repository.CategoryRepository;
import report.category.vo.CategoryVo;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@AutoConfigureTestDatabase
@Import(QueryDslConfiguration.class)
@Slf4j
public class CategoryQueryRepositoryTest {

    @Autowired
    JPAQueryFactory queryFactory;

    @Autowired
    private CategoryRepository categoryRepository;

    private CategoryEntity categoryEntity;

    private CategoryEntity parentCategoryEntity;

    @BeforeEach
    void setup(){
        categoryEntity = CategoryEntity
                .builder()
                .categoryNm("카테고리명 1")
                .build();

        parentCategoryEntity = CategoryEntity
                .builder()
                .categoryNm("부모 카테고리명 1")
                .build();
    }

    @Test
    void 카테고리_저장(){

        //카테고리 저장
        var result = categoryRepository.save(categoryEntity);

        //카테고리 저장후 entity null 여부 테스트
        assertNotNull(result);
        //저장된 카테고리 조회 null 여부 테스트
        Assertions.assertThat(categoryEntity.getCategoryNm()).isEqualTo(result.getCategoryNm());
    }

    @Test
    void 카테고리_저장_수정시_orderNo_카운팅후_증가하여_orderNo_세팅(){

        //카테고리 저장
        var result = categoryRepository.save(categoryEntity);

        //두번째 카테고리 저장
        var resultTwice = categoryRepository.save(CategoryEntity.builder().categoryNm("카테고리명 2").build());

        //부모 카테고리 저장
        var parentResult = categoryRepository.save(parentCategoryEntity);

        //첫번째 카테고리 수정
        result.setParent(parentResult);
        result.changeDepth(parentResult.getDepth() + 1);
        result.changeOrderNo(maxOrderNo(parentResult.getId(), result.getDepth()).intValue());
        var updated = categoryRepository.save(result);

        //두번째 카테고리 수정
        resultTwice.setParent(parentResult);
        resultTwice.changeDepth(parentResult.getDepth() + 1);
        resultTwice.changeOrderNo(maxOrderNo(parentResult.getId(), result.getDepth()).intValue());
        var updatedTwice = categoryRepository.save(resultTwice);


        //카테고리 저장후 entity null 여부 테스트
        assertNotNull(result);
        assertNotNull(resultTwice);

        //카테고리 저장, 수정후 값 검증
        Assertions.assertThat(categoryEntity.getCategoryNm()).isEqualTo(result.getCategoryNm());
        Assertions.assertThat(updated.getParentCategory().getCategoryNm()).isEqualTo(parentResult.getCategoryNm());
        Assertions.assertThat(updated.getOrderNo()).isEqualTo(1);
        Assertions.assertThat(updated.getDepth()).isEqualTo(2);
        Assertions.assertThat(updatedTwice.getOrderNo()).isEqualTo(2);
        Assertions.assertThat(updatedTwice.getDepth()).isEqualTo(2);
    }

    @Test
    void 카테고리_orderNo_재정렬_업데이트(){

        //카테고리 저장
        categoryRepository.save(CategoryEntity.builder().categoryNm("카테고리1").orderNo(1).build());
        categoryRepository.save(CategoryEntity.builder().categoryNm("카테고리2").orderNo(2).build());
        categoryRepository.save(CategoryEntity.builder().categoryNm("카테고리3").orderNo(4).build());
        categoryRepository.save(CategoryEntity.builder().categoryNm("카테고리4").orderNo(8).build());
        categoryRepository.save(CategoryEntity.builder().categoryNm("카테고리5").orderNo(9).build());

        //1depth 카테고리 전체 조회후 orderNo 재정렬 업데이트 로직 시작
        int orderNo = 1;
        for (CategoryApiDto tmpDto: findAllcategorys(new CategoryVo())){
            //자식 Category 루프문 순회하여 orderNo 재정렬
            tmpDto.setOrderNo(orderNo);
            orderNo++;
            updateCategoryOrderNo(tmpDto.getId(), tmpDto.getOrderNo());
        }

        var result = findAllcategorys(new CategoryVo());

        //카테고리 orderNo 정렬 검증
        Assertions.assertThat(result.get(0).getOrderNo()).isEqualTo(1);
        Assertions.assertThat(result.get(1).getOrderNo()).isEqualTo(2);
        Assertions.assertThat(result.get(2).getOrderNo()).isEqualTo(3);
        Assertions.assertThat(result.get(3).getOrderNo()).isEqualTo(4);
        Assertions.assertThat(result.get(4).getOrderNo()).isEqualTo(5);
    }

    @Test
    void 전체_카테고리_조회(){


        //카테고리 저장
        categoryRepository.save(CategoryEntity.builder().categoryNm("카테고리1").build());
        categoryRepository.save(CategoryEntity.builder().categoryNm("카테고리2").build());
        categoryRepository.save(CategoryEntity.builder().categoryNm("카테고리3").build());
        categoryRepository.save(CategoryEntity.builder().categoryNm("카테고리4").build());
        categoryRepository.save(CategoryEntity.builder().categoryNm("카테고리5").build());

        //1depth 카테고리 전체 조회
        var result = findAllcategorys(new CategoryVo());


        //카테고리 사이즈 체크
        Assertions.assertThat(result.size()).isEqualTo(5);
    }

    @Test
    void 자식_카테고리_전체_조회(){

        List<CategoryEntity> addEntity = Arrays.asList(
                CategoryEntity.builder().categoryNm("카테고리1").build(),
                CategoryEntity.builder().categoryNm("카테고리2").build(),
                CategoryEntity.builder().categoryNm("카테고리3").build(),
                CategoryEntity.builder().categoryNm("카테고리4").build(),
                CategoryEntity.builder().categoryNm("카테고리5").build()
        );

        //카테고리 저장
        categoryRepository.save(CategoryEntity.builder().categoryNm("카테고리1").build());
        categoryRepository.save(CategoryEntity.builder().categoryNm("카테고리2").build());
        categoryRepository.save(CategoryEntity.builder().categoryNm("카테고리3").build());
        categoryRepository.save(CategoryEntity.builder().categoryNm("카테고리4").build());
        categoryRepository.save(CategoryEntity.builder().categoryNm("카테고리5").build());

        //1depth 카테고리 전체 조회
        var result = findAllcategorys(new CategoryVo());

        //자식 카테고리 저장
        categoryRepository.save(CategoryEntity.builder().categoryNm("자식 카테고리1").parentCategory(
                CategoryEntity.builder().id(result.get(0).getId()).build()
        ).build());
        categoryRepository.save(CategoryEntity.builder().categoryNm("자식 카테고리2").parentCategory(
                CategoryEntity.builder().id(result.get(0).getId()).build()
        ).build());
        categoryRepository.save(CategoryEntity.builder().categoryNm("자식 카테고리3").parentCategory(
                CategoryEntity.builder().id(result.get(1).getId()).build()
        ).build());
        categoryRepository.save(CategoryEntity.builder().categoryNm("자식 카테고리4").parentCategory(
                CategoryEntity.builder().id(result.get(2).getId()).build()
        ).build());
        categoryRepository.save(CategoryEntity.builder().categoryNm("자식 카테고리5").parentCategory(
                CategoryEntity.builder().id(result.get(3).getId()).build()
        ).build());

        var childResult = findAllChildCategorys(result.stream().map(CategoryApiDto::getId).collect(Collectors.toList()));

        //카테고리 사이즈 체크
        Assertions.assertThat(childResult.size()).isEqualTo(5);
        //자식카테고리 부모 카테고리명 일치 검증
        Assertions.assertThat(childResult.get(0).getParentCategory().getCategoryNm()).isEqualTo("카테고리1");
        Assertions.assertThat(childResult.get(1).getParentCategory().getCategoryNm()).isEqualTo("카테고리1");
        Assertions.assertThat(childResult.get(2).getParentCategory().getCategoryNm()).isEqualTo("카테고리2");
    }

    @Test
    void 상위_카테고리_조회(){

        //카테고리 저장
        var result = categoryRepository.save(categoryEntity);

        //카테고리 조회
        var findCategory = findCategoryOne(result.getId());


        //카테고리 저장후 entity null 여부 테스트
        assertNotNull(result);
        //저장된 카테고리 조회 entity null 여부 테스트
        assertNotNull(findCategory);
        //파라미터 entity , 등록 후 반환된 entity 값 비교
        Assertions.assertThat(categoryEntity.getCategoryNm()).isEqualTo(result.getCategoryNm());
        //등록 후 반환된 entity, 등록 후 조회한 entity 값 비교
        Assertions.assertThat(result.getCategoryNm()).isEqualTo(findCategory.getCategoryNm());
    }

    @Test
    void 카테고리_수정(){

        //부모 카테고리 저장
        var parentResult = categoryRepository.save(parentCategoryEntity);

        //부모 카테고리 entity 세팅
        categoryEntity.setParent(parentResult);
        //카테고리 저장
        var result = categoryRepository.save(categoryEntity);

        //카테고리 조회
        var findCategory = findCategoryOne(result.getId());

        //부모 카테고리 변경
        updateCategory(parentResult.getId(), CategoryDto.builder().id(parentResult.getId()).categoryNm("부모 카테고리명 변경1").build());

        //자식 카테고리 변경
        updateCategory(result.getId(), CategoryDto.builder().id(result.getId()).categoryNm("카테고리명 변경1").build());

        //변경후 카테고리 조회
        var updatedCategory = findCategoryOne(result.getId());


        //카테고리 저장후 entity, 부모 entity null 여부 테스트
        assertNotNull(result);
        assertNotNull(result.getParentCategory().getCategoryNm());
        //저장된 카테고리 조회 entity null 여부 테스트
        assertNotNull(findCategory);
        //파라미터 entity , 등록 후 반환된 entity 값 비교
        Assertions.assertThat(categoryEntity.getCategoryNm()).isEqualTo(result.getCategoryNm());
        //등록 후 반환된 entity, 등록 후 조회한 entity 값 비교
        Assertions.assertThat(result.getCategoryNm()).isEqualTo(findCategory.getCategoryNm());
        //카테고리 수정후 값 검증
        Assertions.assertThat(updatedCategory.getCategoryNm()).isEqualTo("카테고리명 변경1");
        Assertions.assertThat(updatedCategory.getParentCategory().getCategoryNm()).isEqualTo("부모 카테고리명 변경1");
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
    public CategoryApiDto findCategoryOne(Long id){
        var QcategoryEntity = QCategoryEntity.categoryEntity;
        var QparentCategoryEntity = new QCategoryEntity("parentCategoryEntity");

        return this.queryFactory
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
                        QcategoryEntity.id.eq(id),
                        QcategoryEntity.deleteFlag.eq("N")
                )
                .orderBy(QcategoryEntity.orderNo.asc())
                .fetchOne();
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

    private BooleanExpression parentCategoryId(Long parentCategoryId, int depth) {
        var categoryEntity = QCategoryEntity.categoryEntity;
        return depth > 1 ? categoryEntity.parentCategory.id.eq(parentCategoryId) : null;
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
}
