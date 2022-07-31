package report.category;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import report.category.dto.CategoryApiDto;
import report.category.dto.CategoryDto;
import report.category.entity.CategoryEntity;
import report.category.exception.CategoryException;
import report.category.exception.ErrorCode;
import report.category.repository.CategoryQueryRepository;
import report.category.repository.CategoryRepository;
import report.category.vo.CategoryVo;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@Slf4j
class CategoryApplicationTests {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryQueryRepository categoryQueryRepository;


    @Test
    void 카테고리_전체조회() {

        List<CategoryApiDto> data = new ArrayList<>();
        data.add(CategoryApiDto.builder().categoryNm("카테고리명").build());
        given(categoryQueryRepository.findAllcategorys(any())).willReturn(data);


        //전체 Category 조회
        var result = categoryQueryRepository.findAllcategorys(CategoryVo.builder().build());

        //하위 depth Category 조회하여 세팅해준 후 리턴
        //setChildCategory(result);

        assertNotNull(result);
    }


    @Nested
    @DisplayName("카테고리 저장 로직")
    class categorySave{

        private CategoryDto dto;
        private CategoryEntity categoryEntity;
        private CategoryEntity setupParentEntity;
        private CategoryEntity findCategoryEntity;

        @BeforeEach
        void save_setup(){
            //dto = CategoryDto.builder().categoryNm("카테고리명").build();
            dto = CategoryDto.builder().categoryNm("카테고리명").parentCategory(CategoryDto.builder().id(1L).categoryNm("부모 카테고리").build()).build();
            categoryEntity = CategoryEntity
                    .builder()
                    .categoryNm(dto.getCategoryNm())
                    .orderNo(0)
                    .deleteFlag("N")
                    .depth(1)
                    .build();

            setupParentEntity = CategoryEntity
                    .builder()
                    .id(1L)
                    .categoryNm("부모 카테고리")
                    .orderNo(1)
                    .deleteFlag("N")
                    .depth(1)
                    .build();
        }

        @Test
        void 카테고리_저장_부모카테고리_미포함() {
            dto = CategoryDto.builder().categoryNm("카테고리명").build();

            given(categoryRepository.save(any(CategoryEntity.class))).willReturn(categoryEntity);
            given(categoryQueryRepository.maxOrderNo(anyLong(), anyInt())).willReturn(0L);

            //부모 Category 가 존재할시 해당 부모 Category 하위에서  orderNo 증가를 위한
            //부모 Category id 저장 변수
            Long parentCategoryId = 0L;

            //파라미터로 넘어온 dto 부모 객체가 null이 아닐경우
            if(null != dto.getParentCategory()) {
                //부모 Category 조회
                findCategoryEntity = categoryRepository.findByIdAndDeleteFlag(dto.getParentCategory().getId(), "N").orElseThrow(() -> new CategoryException(ErrorCode.CATEGORY_NOT_FOUND));
                //부모 Category id 저장
                parentCategoryId = findCategoryEntity.getId();
                //부모 Category 의 (depth + 1) 저장
                categoryEntity.changeDepth(findCategoryEntity.getDepth() + 1);
                //부모 Category 저장
                categoryEntity.setParent(findCategoryEntity);
            }

            //같은 depth의 max orderNo에서 1 증가하여 세팅
            categoryEntity.changeOrderNo(categoryQueryRepository.maxOrderNo(parentCategoryId, categoryEntity.getDepth()).intValue() + 1);
            var resultEntity = categoryRepository.save(categoryEntity);


            Assertions.assertThat(categoryEntity.getCategoryNm()).isEqualTo(resultEntity.getCategoryNm());
            assertNull(resultEntity.getParentCategory());
            Assertions.assertThat(1).isEqualTo(resultEntity.getOrderNo());
        }

        @Test
        void 카테고리_저장_부모카테고리_포함() {

            given(categoryRepository.save(any(CategoryEntity.class))).willReturn(categoryEntity);
            given(categoryRepository.findByIdAndDeleteFlag(anyLong(), anyString())).willReturn(Optional.of(setupParentEntity));
            given(categoryQueryRepository.maxOrderNo(anyLong(), anyInt())).willReturn(0L);

            //부모 Category 가 존재할시 해당 부모 Category 하위에서  orderNo 증가를 위한
            //부모 Category id 저장 변수
            Long parentCategoryId = 0L;

            //파라미터로 넘어온 dto 부모 객체가 null이 아닐경우
            if(null != dto.getParentCategory()) {
                //부모 Category 조회
                findCategoryEntity = categoryRepository.findByIdAndDeleteFlag(dto.getParentCategory().getId(), "N").orElseThrow(() -> new CategoryException(ErrorCode.CATEGORY_NOT_FOUND));
                //부모 Category id 저장
                parentCategoryId = findCategoryEntity.getId();
                //부모 Category 의 (depth + 1) 저장
                categoryEntity.changeDepth(findCategoryEntity.getDepth() + 1);
                //부모 Category 저장
                categoryEntity.setParent(findCategoryEntity);
            }

            //같은 depth의 max orderNo에서 1 증가하여 세팅
            categoryEntity.changeOrderNo(categoryQueryRepository.maxOrderNo(parentCategoryId, categoryEntity.getDepth()).intValue() + 1);
            var resultEntity = categoryRepository.save(categoryEntity);


            Assertions.assertThat(categoryEntity.getCategoryNm()).isEqualTo(resultEntity.getCategoryNm());
            Assertions.assertThat(findCategoryEntity.getCategoryNm()).isEqualTo(resultEntity.getParentCategory().getCategoryNm());
            Assertions.assertThat(1).isEqualTo(resultEntity.getOrderNo());
        }

        @Test
        void 카테고리_부모_저장전_부모_조회_값이존재하지_않는_오류(){
            given(categoryRepository.findByIdAndDeleteFlag(anyLong(), anyString())).willReturn(Optional.empty());

            assertThrows(CategoryException.class, () -> {
                //부모 Category 조회
                findCategoryEntity = categoryRepository.findByIdAndDeleteFlag(dto.getParentCategory().getId(), "N").orElseThrow(() -> new CategoryException(ErrorCode.CATEGORY_NOT_FOUND));
            });
        }
    }


    @Nested
    @DisplayName("카테고리 수정 로직")
    class categoryModify {

        private Long id;
        private CategoryDto dto;
        private CategoryEntity categoryEntity;
        private CategoryEntity pagrentCategoryEntity;
        private CategoryEntity prevPagrentCategoryEntity;
        private CategoryEntity findPagrentCategoryEntity;

        @BeforeEach
        void save_setup() {
            id = 2L;
            dto = CategoryDto.builder().categoryNm("카테고리명 변경").parentCategory(CategoryDto.builder().id(1L).build()).build();
            categoryEntity = CategoryEntity
                    .builder()
                    .id(2L)
                    .categoryNm("카테고리 명")
                    .orderNo(3)
                    .deleteFlag("N")
                    .depth(2)
                    .build();

            pagrentCategoryEntity = CategoryEntity
                    .builder()
                    .id(1L)
                    .categoryNm("1.부모 카테고리")
                    .orderNo(1)
                    .deleteFlag("N")
                    .depth(1)
                    .build();

            prevPagrentCategoryEntity = CategoryEntity
                    .builder()
                    .id(3L)
                    .categoryNm("2.부모 카테고리")
                    .orderNo(1)
                    .deleteFlag("N")
                    .depth(1)
                    .build();
        }

        @Test
        void 카테고리_수정_부모객체_포함(){

            given(categoryRepository.findById(anyLong())).willReturn(Optional.of(categoryEntity));
            //given(categoryRepository.findById(anyLong())).willReturn(Optional.of(pagrentCategoryEntity));
            given(categoryQueryRepository.maxOrderNo(anyLong(),anyInt())).willReturn(1L);

            //수정할 Category 조회
            var findCategoryEntity = categoryRepository.findById(id).orElseThrow(() -> new CategoryException(ErrorCode.CATEGORY_NOT_FOUND));

            //부모 Category 값이 비어있지 않을시 수정
            if(null !=  dto.getParentCategory()){
                //부모 Category 조회
                findPagrentCategoryEntity = categoryRepository.findById(dto.getParentCategory().getId()).orElseThrow(() -> new CategoryException(ErrorCode.CATEGORY_NOT_FOUND));

                //같은 depth의 max orderNo에서 1 증가하여 세팅
                dto.setOrderNo(categoryQueryRepository.maxOrderNo(pagrentCategoryEntity.getId(), pagrentCategoryEntity.getDepth() + 1).intValue() + 1);
            }

            Assertions.assertThat(categoryEntity.getCategoryNm()).isEqualTo(findCategoryEntity.getCategoryNm());
            Assertions.assertThat(1L).isNotEqualTo(dto.getOrderNo());

        }
    }




    /*
     * 하위 depth 자식 Category 조회 (재귀함수)
     * */
    public List<CategoryApiDto> setChildCategory(List<CategoryApiDto> parentCategoryList) throws CategoryException {

        //파라미터로 넘어온 부모객체의 id 값만 뽑아 자식 Category 조회
        var childResult = categoryQueryRepository.findAllChildCategorys(parentCategoryList.stream().map(CategoryApiDto::getId).collect(Collectors.toList()));

        //현 Category에서 자식 Category가 존재하지 않을시 재귀 종료
        if(childResult.isEmpty()){
            return parentCategoryList;
        }

        //부모 Category entity에 자식 Category List 저장
        parentCategoryList = parentCategoryList.stream().map(parentCategoryData -> {
                    parentCategoryData.setChildCategoryList(childResult.stream().filter(childCategoryData -> parentCategoryData.getId() == childCategoryData.getParentCategory().getId()).collect(Collectors.toList()));
                    return parentCategoryData;
                }
        ).collect(Collectors.toList());

        //재귀 종료 전까지 지속해서 하위 depth 자식 Category 조회
        setChildCategory(childResult);

        //재귀문 종료시 파라미터로 넘어온 부모 Category 리턴
        return parentCategoryList;
    }

}
