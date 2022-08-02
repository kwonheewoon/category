package report.category;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import report.category.dto.CategoryApiDto;
import report.category.dto.CategoryDto;
import report.category.entity.CategoryEntity;
import report.category.exception.CategoryException;
import report.category.exception.ErrorCode;
import report.category.repository.CategoryQueryRepository;
import report.category.repository.CategoryRepository;
import report.category.vo.CategoryVo;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@Slf4j
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryQueryRepository categoryQueryRepository;




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


            //같은 depth의 max orderNo에서 1 증가하여 세팅
            categoryEntity.changeOrderNo(categoryQueryRepository.maxOrderNo(0L, categoryEntity.getDepth()).intValue() + 1);
            var resultEntity = categoryRepository.save(categoryEntity);


            Assertions.assertThat(categoryEntity.getCategoryNm()).isEqualTo(resultEntity.getCategoryNm());
            assertNull(resultEntity.getParentCategory());
            Assertions.assertThat(1).isEqualTo(resultEntity.getOrderNo());

            verify(categoryRepository).save(refEq(categoryEntity));
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

            verify(categoryRepository).save(categoryEntity);
            verify(categoryRepository).findByIdAndDeleteFlag(dto.getParentCategory().getId(), "N");
            verify(categoryQueryRepository).maxOrderNo(parentCategoryId, categoryEntity.getDepth());
        }

        @Test
        void 카테고리_부모_저장전_부모_조회_값이존재하지_않는_오류(){
            given(categoryRepository.findByIdAndDeleteFlag(anyLong(), anyString())).willReturn(Optional.empty());

            assertThrows(CategoryException.class, () -> {
                //부모 Category 조회
                findCategoryEntity = categoryRepository.findByIdAndDeleteFlag(dto.getParentCategory().getId(), "N").orElseThrow(() -> new CategoryException(ErrorCode.CATEGORY_NOT_FOUND));
            });

            verify(categoryRepository).findByIdAndDeleteFlag(dto.getParentCategory().getId(), "N");
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
            dto = CategoryDto.builder().categoryNm("변경 카테고리명").parentCategory(CategoryDto.builder().id(1L).build()).build();
            categoryEntity = CategoryEntity
                    .builder()
                    .id(2L)
                    .categoryNm("변경전 카테고리 명")
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

            verify(categoryRepository).findById(1L);
            verify(categoryQueryRepository).maxOrderNo(dto.getParentCategory().getId(), pagrentCategoryEntity.getDepth() + 1);
        }

        @Test
        void 카테고리_수정_부모객체_미포함(){

            given(categoryRepository.findById(anyLong())).willReturn(Optional.of(categoryEntity));

            //수정할 Category 조회
            var findCategoryEntity = categoryRepository.findById(id).orElseThrow(() -> new CategoryException(ErrorCode.CATEGORY_NOT_FOUND));


            Assertions.assertThat(categoryEntity.getCategoryNm()).isEqualTo(findCategoryEntity.getCategoryNm());
            Assertions.assertThat(3).isEqualTo(findCategoryEntity.getOrderNo());

            verify(categoryRepository).findById(id);
        }

        @Test
        void 카테고리_수정과_부모객체_수정시_수정전_현_depth_카테고리_orderNo_재정렬(){
            categoryEntity.changeParentCategory(pagrentCategoryEntity);
            given(categoryRepository.findById(anyLong())).willReturn(Optional.of(categoryEntity));
            given(categoryQueryRepository.findAllChildCategorys(any())).willReturn(
                    Arrays.asList(
                            new CategoryApiDto(5L, "자식 카테고리5", 1, "N", LocalDateTime.now(), LocalDateTime.now()),
                            new CategoryApiDto(6L, "자식 카테고리6", 2, "N", LocalDateTime.now(), LocalDateTime.now()),
                            new CategoryApiDto(7L, "자식 카테고리7", 5, "N", LocalDateTime.now(), LocalDateTime.now()),
                            new CategoryApiDto(8L, "자식 카테고리8", 6, "N", LocalDateTime.now(), LocalDateTime.now())
                    )
            );

            //수정할 Category 조회
            var findCategoryEntity = categoryRepository.findById(id).orElseThrow(() -> new CategoryException(ErrorCode.CATEGORY_NOT_FOUND));


            //Category 수정이 정상이고 파라미터로 넘어온 dto에 부모객체가 존재할시
            var updateResult = 1;
            List<CategoryApiDto> tmpList = new ArrayList<>();
            if(updateResult > 0 && null != dto.getParentCategory() && null != findCategoryEntity.getParentCategory()){
                int orderNo = 1;
                //수정전 죄회한 Category entity에서 부모 Category id를 이용하여 자식 Category 모두 조회
                for (CategoryApiDto tmpDto : categoryQueryRepository.findAllChildCategorys(Collections.singletonList(findCategoryEntity.getParentCategory().getId()))) {
                    //자식 Category 루프문 순회하여 orderNo 재정렬
                    tmpDto.setOrderNo(orderNo);
                    tmpList.add(tmpDto);
                    orderNo++;
                }
            }

            Assertions.assertThat(categoryEntity.getCategoryNm()).isEqualTo(findCategoryEntity.getCategoryNm());
            Assertions.assertThat(1).isEqualTo(tmpList.get(0).getOrderNo());
            Assertions.assertThat(2).isEqualTo(tmpList.get(1).getOrderNo());
            Assertions.assertThat(3).isEqualTo(tmpList.get(2).getOrderNo());
            Assertions.assertThat(4).isEqualTo(tmpList.get(3).getOrderNo());

            verify(categoryRepository).findById(id);
            verify(categoryQueryRepository).findAllChildCategorys(Collections.singletonList(findCategoryEntity.getParentCategory().getId()));
        }
    }


    @Nested
    @DisplayName("카테고리 조회 로직")
    class categoryList{

        List<CategoryApiDto> parentCategoryList = new ArrayList<>();
        List<CategoryApiDto> childCategoryList = new ArrayList<>();

        @BeforeEach
        void setup(){
            parentCategoryList = Arrays.asList(new CategoryApiDto(5L, "부모 카테고리5", 1, "N", LocalDateTime.now(), LocalDateTime.now()),
                    new CategoryApiDto(6L, "부모 카테고리6", 2, "N", LocalDateTime.now(), LocalDateTime.now()),
                    new CategoryApiDto(7L, "부모 카테고리7", 3, "N", LocalDateTime.now(), LocalDateTime.now()),
                    new CategoryApiDto(8L, "부모 카테고리8", 4, "N", LocalDateTime.now(), LocalDateTime.now()));

            childCategoryList = Arrays.asList(
                    CategoryApiDto.builder().id(9L).categoryNm("자식 카테고리1").orderNo(1).parentCategory(new CategoryApiDto.ParentCategoryApiDto(5L, "부모 카테고리5")).build(),
                    CategoryApiDto.builder().id(10L).categoryNm("자식 카테고리2").orderNo(2).parentCategory(new CategoryApiDto.ParentCategoryApiDto(5L, "부모 카테고리5")).build(),
                    CategoryApiDto.builder().id(11L).categoryNm("자식 카테고리3").orderNo(3).parentCategory(new CategoryApiDto.ParentCategoryApiDto(6L, "부모 카테고리6")).build(),
                    CategoryApiDto.builder().id(12L).categoryNm("자식 카테고리4").orderNo(4).parentCategory(new CategoryApiDto.ParentCategoryApiDto(6L, "부모 카테고리6")).build()
            );

            /*parentCategoryList = parentCategoryList.stream().map(
                    data -> {
                        childCategoryList.stream().filter(childData -> childData.getParentCategory().getId() == data.getId()).collect(Collectors.toList()).forEach(
                                setData -> data.getChildCategoryList().add(setData)
                        );
                        return data;
                    }
            ).collect(Collectors.toList());*/
        }

        @Test
        void 카테고리_전체조회() {
            given(categoryQueryRepository.findAllcategorys(any())).willReturn(parentCategoryList);

            var result = categoryQueryRepository.findAllcategorys(CategoryVo.builder().build());

            assertNotNull(result);

            verify(categoryQueryRepository).findAllcategorys(refEq(new CategoryVo()));
        }

        @Test
        void 상위_카테고리_상세조회(){
            given(categoryQueryRepository.findCategoryOne(anyLong())).willReturn(Optional.ofNullable(new CategoryApiDto(5L, "부모 카테고리5", 1, "N", LocalDateTime.now(), LocalDateTime.now())));

            var result = categoryQueryRepository.findCategoryOne(5L).orElseThrow();

            assertNotNull(result);
            Assertions.assertThat(result.getId()).isEqualTo(5L);
            Assertions.assertThat(result.getCategoryNm()).isEqualTo("부모 카테고리5");

            verify(categoryQueryRepository).findCategoryOne(5L);
        }

        @Test
        void 하위_depth_자식_Category_조회(){

            given(categoryQueryRepository.findAllChildCategorys(any())).willReturn(childCategoryList);

            //파라미터로 넘어온 부모객체의 id 값만 뽑아 자식 Category 조회
            var childResult = categoryQueryRepository.findAllChildCategorys(parentCategoryList.stream().map(CategoryApiDto::getId).collect(Collectors.toList()));


            //부모 Category entity에 자식 Category List 저장
            parentCategoryList = parentCategoryList.stream().map(parentCategoryData -> {
                        parentCategoryData.setChildCategoryList(childResult.stream().filter(childCategoryData -> parentCategoryData.getId() == childCategoryData.getParentCategory().getId()).collect(Collectors.toList()));
                        return parentCategoryData;
                    }
            ).collect(Collectors.toList());

            parentCategoryList.stream().forEach(
                    data -> {
                        data.getChildCategoryList().stream().forEach(
                                childData -> {
                                    Assertions.assertThat(data.getId()).isEqualTo(childData.getParentCategory().getId());
                                }
                        );
                    }
            );

            verify(categoryQueryRepository).findAllChildCategorys(parentCategoryList.stream().map(CategoryApiDto::getId).collect(Collectors.toList()));
        }

        @Test
        void 하위_depth_자식_Category_조회_재귀함수_테스트(){

            var result = setChildCategory(parentCategoryList);

            Assertions.assertThat(result.get(0).getChildCategoryList().size()).isEqualTo(2);
            Assertions.assertThat(result.get(1).getChildCategoryList().size()).isEqualTo(2);
            Assertions.assertThat(result.get(2).getChildCategoryList().size()).isEqualTo(0);
            Assertions.assertThat(result.get(3).getChildCategoryList().size()).isEqualTo(0);

            verify(categoryQueryRepository).findAllChildCategorys(parentCategoryList.stream().map(CategoryApiDto::getId).collect(Collectors.toList()));
        }
    }

    /*
     * 하위 depth 자식 Category 조회 (재귀함수)
     * */
    public List<CategoryApiDto> setChildCategory(List<CategoryApiDto> parentCategoryList) throws CategoryException {

        var childCategoryList = Arrays.asList(
                CategoryApiDto.builder().id(9L).categoryNm("자식 카테고리1").orderNo(1).parentCategory(new CategoryApiDto.ParentCategoryApiDto(5L, "부모 카테고리5")).build(),
                CategoryApiDto.builder().id(10L).categoryNm("자식 카테고리2").orderNo(2).parentCategory(new CategoryApiDto.ParentCategoryApiDto(5L, "부모 카테고리5")).build(),
                CategoryApiDto.builder().id(11L).categoryNm("자식 카테고리3").orderNo(3).parentCategory(new CategoryApiDto.ParentCategoryApiDto(6L, "부모 카테고리6")).build(),
                CategoryApiDto.builder().id(12L).categoryNm("자식 카테고리4").orderNo(4).parentCategory(new CategoryApiDto.ParentCategoryApiDto(6L, "부모 카테고리6")).build()
        );

        //2depth 자식 Category 조회후 재귀 종료를 위해 추가
        if(parentCategoryList.isEmpty()){
            return parentCategoryList;
        }

        given(categoryQueryRepository.findAllChildCategorys(any())).willReturn(childCategoryList);

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
        //2depth 자식 Category 조회후 재귀 종료를 위해 빈 List 전달
        setChildCategory(Arrays.asList());

        //재귀문 종료시 파라미터로 넘어온 부모 Category 리턴
        return parentCategoryList;
    }

    @Nested
    @DisplayName("카테고리 삭제 로직")
    class CategoryDelete {

        @Test
        void 카테고리_삭제(){

            given(categoryRepository.findById(any())).willReturn(Optional.of(CategoryEntity.builder().id(9L).categoryNm("자식 카테고리1").orderNo(1).build()));

            //삭제할 Category 조회
            var findCategoryEntity = categoryRepository.findById(9L).orElseThrow(() -> new CategoryException(ErrorCode.CATEGORY_NOT_FOUND));

            //Category 삭제
            categoryRepository.deleteById(findCategoryEntity.getId());

            verify(categoryRepository).findById(9L);
        }

        @Test
        void 카테고리_삭제시_조회할_데이터_없는_오류(){

            given(categoryRepository.findById(any())).willReturn(Optional.empty());



            assertThrows(CategoryException.class, () -> {
                        //삭제할 Category 조회
                        var findCategoryEntity = categoryRepository.findById(9L).orElseThrow(() -> new CategoryException(ErrorCode.CATEGORY_NOT_FOUND));

                        //Category 삭제
                        categoryRepository.deleteById(findCategoryEntity.getId());


                    });
            verify(categoryRepository).findById(9L);
        }
    }

}
