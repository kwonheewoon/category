package report.category;


import org.assertj.core.api.Assertions;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.internal.matchers.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;
import report.category.dto.CategoryDto;
import report.category.entity.CategoryEntity;
import report.category.repository.CategoryQueryRepository;
import report.category.repository.CategoryRepository;
import report.category.service.CategoryService;
import report.category.vo.CategoryVo;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


/**
    *
    * 테스트용 @EnableJpaAuditing configuration이 설정되어 있어
    * report.category.CategoryApplication.java -> @EnableJpaAuditing 주석처리후 통합테스트 실행
    *
* */


@Transactional
@SpringBootTest
public class CategoryIntegrationTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryQueryRepository categoryQueryRepository;



    @Nested
    @DisplayName("카테고리 조회 통합테스트")
    class CategoryFind{

        private CategoryEntity parent;

        @BeforeEach
        void setup(){
            //카테고리 저장
            parent = categoryRepository.save(CategoryEntity.builder().categoryNm("카테고리1").orderNo(1).build());
            categoryRepository.save(CategoryEntity.builder().categoryNm("카테고리2").orderNo(2).build());
            categoryRepository.save(CategoryEntity.builder().categoryNm("카테고리3").orderNo(4).build());
            categoryRepository.save(CategoryEntity.builder().categoryNm("카테고리4").orderNo(8).build());
            categoryRepository.save(CategoryEntity.builder().categoryNm("카테고리5").orderNo(9).build());

            var parentTwice = categoryRepository.save(CategoryEntity.builder().categoryNm("자식 카테고리1").depth(2).parentCategory(CategoryEntity.builder().id(parent.getId()).build()).orderNo(1).build());
            categoryRepository.save(CategoryEntity.builder().categoryNm("자식 카테고리1").depth(2).parentCategory(CategoryEntity.builder().id(parent.getId()).build()).orderNo(1).build());

            categoryRepository.save(CategoryEntity.builder().categoryNm("자식 카테고리 1 3depth").depth(3).parentCategory(CategoryEntity.builder().id(parentTwice.getId()).build()).orderNo(1).build());
            categoryRepository.save(CategoryEntity.builder().categoryNm("자식 카테고리 2 3depth").depth(3).parentCategory(CategoryEntity.builder().id(parentTwice.getId()).build()).orderNo(1).build());
        }

        @Test
        void 전체_Category_조회(){
            //given
            CategoryVo vo = new CategoryVo();

            //when
            var resultList = categoryService.categoryList(vo);

            //then
            Assertions.assertThat(resultList.size()).isEqualTo(5);
        }

        @Test
        void 상위_Category_조회(){

            //given
            Long id = parent.getId();

            //when
            var result = categoryService.find(id);

            //then
            //상위 카테고리 1개 검증
            assertNotNull(result);
            //상위 카테고리 -> 자식 카테고리 2개 검증
            Assertions.assertThat(result.getChildCategoryList().size()).isEqualTo(2);
            //3depth 자식 카테고리 2개 검증
            Assertions.assertThat(result.getChildCategoryList().get(0).getChildCategoryList().size()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("카테고리 저장 통합테스트")
    class categorySave{

        private CategoryEntity parent;

        @BeforeEach
        void setup(){
            //카테고리 저장
            parent = categoryRepository.save(CategoryEntity.builder().categoryNm("부모 카테고리").orderNo(1).build());
        }

        @Test
        void 카테고리_저장(){
            //given
            CategoryDto dto = CategoryDto.builder().categoryNm("카테고리1").build();

            //when
            var saveCategory = categoryService.saveCategory(dto);

            //then
            //저장된 카테고리명 검증
            Assertions.assertThat(saveCategory.getCategoryNm()).isEqualTo(dto.getCategoryNm());
        }

        @Test
        void 카테고리_저장_부모객체포함(){
            //given
            CategoryDto dto = CategoryDto.builder().categoryNm("카테고리1").parentCategory(CategoryDto.builder().id(parent.getId()).build()).build();

            //when
            var saveCategory = categoryService.saveCategory(dto);

            //then
            //저장된 카테고리 값 검증
            Assertions.assertThat(saveCategory.getCategoryNm()).isEqualTo(dto.getCategoryNm());
            Assertions.assertThat(saveCategory.getParentCategory().getCategoryNm()).isEqualTo("부모 카테고리");
        }
    }


    @Nested
    @DisplayName("카테고리 수정 통합테스트")
    class categoryModify{
        private CategoryEntity parent;

        private CategoryEntity parentTwice;

        private CategoryEntity child;

        @BeforeEach
        void setup(){
            //부모 카테고리 저장
            parent = categoryRepository.save(CategoryEntity.builder().categoryNm("부모 카테고리").orderNo(1).build());
            parentTwice = categoryRepository.save(CategoryEntity.builder().categoryNm("부모 카테고리2").orderNo(2).build());

            child = categoryRepository.save(CategoryEntity.builder().categoryNm("자식 카테고리1").depth(2).parentCategory(CategoryEntity.builder().id(parent.getId()).build()).orderNo(1).build());
            categoryRepository.save(CategoryEntity.builder().categoryNm("자식 카테고리2").depth(2).parentCategory(CategoryEntity.builder().id(parent.getId()).build()).orderNo(2).build());
            categoryRepository.save(CategoryEntity.builder().categoryNm("자식 카테고리3").depth(2).parentCategory(CategoryEntity.builder().id(parentTwice.getId()).build()).orderNo(1).build());
        }

        @Test
        void 카테고리_수정(){

            //given
            Long id = child.getId();
            CategoryDto dto = CategoryDto.builder().categoryNm("자식 카테고리1 이름 변경").parentCategory(CategoryDto.builder().id(parentTwice.getId()).build()).build();

            //when
            categoryService.modifyCategory(id, dto);

            //then
            var findCategoryEntity = categoryService.find(id);
            //저장된 카테고리 값 검증
            Assertions.assertThat(findCategoryEntity.getCategoryNm()).isEqualTo("자식 카테고리1 이름 변경");
            Assertions.assertThat(findCategoryEntity.getParentCategory().getId()).isEqualTo(parentTwice.getId());
        }
    }

    @Nested
    @DisplayName("카테고리 삭제 통합테스트")
    class categoryDelete{

        private CategoryEntity parent;

        private CategoryEntity parentTwice;

        private CategoryEntity child;

        @BeforeEach
        void setup(){
            //부모 카테고리 저장
            parent = categoryRepository.save(CategoryEntity.builder().categoryNm("부모 카테고리").orderNo(1).build());
            parentTwice = categoryRepository.save(CategoryEntity.builder().categoryNm("부모 카테고리2").orderNo(2).build());

            child = categoryRepository.save(CategoryEntity.builder().categoryNm("자식 카테고리1").depth(2).parentCategory(CategoryEntity.builder().id(parent.getId()).build()).orderNo(1).build());
            categoryRepository.save(CategoryEntity.builder().categoryNm("자식 카테고리2").depth(2).parentCategory(CategoryEntity.builder().id(parent.getId()).build()).orderNo(2).build());
            categoryRepository.save(CategoryEntity.builder().categoryNm("자식 카테고리3").depth(2).parentCategory(CategoryEntity.builder().id(parentTwice.getId()).build()).orderNo(1).build());
        }

        @Test
        void 카테고리_삭제(){

            //given
            Long parentId = parent.getId();
            Long childId = child.getId();

            //when
            //categoryService.deleteCategory(childId);
            //categoryService.deleteCategory(parentId);

            //then
            var findCategoryEntity = categoryService.find(parentId);
            var findChildCategoryEntity = categoryService.find(childId);

            //삭제된 카테고리 값 검증
            assertNotNull(findCategoryEntity);
            //삭제된 카테고리 값 검증
            assertNotNull(findChildCategoryEntity);

        }

    }

}
