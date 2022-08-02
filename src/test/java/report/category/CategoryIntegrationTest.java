package report.category;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;
import report.category.entity.CategoryEntity;
import report.category.repository.CategoryQueryRepository;
import report.category.repository.CategoryRepository;
import report.category.service.CategoryService;
import report.category.vo.CategoryVo;

@Transactional
@SpringBootTest
public class CategoryIntegrationTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryQueryRepository categoryQueryRepository;

    @BeforeEach
    void setup(){
        //카테고리 저장
        categoryRepository.save(CategoryEntity.builder().categoryNm("카테고리1").orderNo(1).build());
        categoryRepository.save(CategoryEntity.builder().categoryNm("카테고리2").orderNo(2).build());
        categoryRepository.save(CategoryEntity.builder().categoryNm("카테고리3").orderNo(4).build());
        categoryRepository.save(CategoryEntity.builder().categoryNm("카테고리4").orderNo(8).build());
        categoryRepository.save(CategoryEntity.builder().categoryNm("카테고리5").orderNo(9).build());
    }

    @Test
    void 전체_Category_조회(){

        CategoryVo vo = new CategoryVo();

        var resultList = categoryService.categoryList(vo);

        Assertions.assertThat(resultList.size()).isEqualTo(5);
    }
}
