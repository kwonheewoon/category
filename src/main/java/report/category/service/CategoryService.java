package report.category.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import report.category.dto.CategoryApiDto;
import report.category.dto.CategoryDto;
import report.category.entity.CategoryEntity;
import report.category.enumclass.CategoryEnum;
import report.category.exception.CategoryException;
import report.category.exception.ErrorCode;
import report.category.repository.CategoryQueryRepository;
import report.category.repository.CategoryRepository;
import report.category.util.SucessResponse;
import report.category.vo.CategoryVo;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    private final CategoryQueryRepository categoryQueryRepository;

    /**
     * 전체 Category 조회
     *   ㄴ하위 Category 조회
     */
    public List<CategoryApiDto> categoryList(CategoryVo vo) throws CategoryException{

        //전체 Category 조회
        var result = categoryQueryRepository.findAllcategorys(vo);
        
        //하위 depth Category 조회하여 세팅해준 후 리턴
        return setChildCategory(result);
    }

    /**
     * 상위 Category 조회
     *   ㄴ하위 Category 조회
     */
    public CategoryApiDto find(Long searchCategoryId) throws CategoryException{

        //상위 Category 조회
        var result = categoryQueryRepository.findCategoryOne(searchCategoryId).orElseThrow(() -> new CategoryException(ErrorCode.CATEGORY_NOT_FOUND));

        //하위 depth Category 조회하여 세팅해준 후 리턴
        //메모리 절약을 위해 싱글톤 arrayList 생성
        return setChildCategory(Collections.singletonList(result)).stream().findFirst().orElseThrow(() -> new CategoryException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    /**
     * Category 저장
     */
    public CategoryApiDto saveCategory(CategoryDto dto){

        //저장할 Category entity 생성
        var categoryEntity = CategoryEntity
                .builder()
                .categoryNm(dto.getCategoryNm())
                .orderNo(dto.getOrderNo())
                .depth(1)
                .build();

        //부모 Category 가 존재할시 해당 부모 Category 하위에서  orderNo 증가를 위한
        //부모 Category id 저장 변수
        long parentCategoryId = 0;

        //파라미터로 넘어온 dto 부모 객체가 null이 아닐경우
        if(!dto.parentCategoryisEmpty()) {
            //부모 Category 조회
            var findCategoryEntity = categoryRepository.findByIdAndDeleteFlag(dto.getParentCategory().getId(), "N").orElseThrow(() -> new CategoryException(ErrorCode.CATEGORY_NOT_FOUND));
            //부모 Category id 저장
            parentCategoryId = findCategoryEntity.getId();
            //부모 Category 의 (depth + 1) 저장
            categoryEntity.changeDepth(findCategoryEntity.getDepth() + 1);
            //부모 Category 저장
            categoryEntity.setParent(findCategoryEntity);
        }

        //같은 depth의 max orderNo에서 1 증가하여 세팅
        categoryEntity.changeOrderNo(categoryQueryRepository.maxOrderNo(parentCategoryId, categoryEntity.getDepth()).intValue() + 1);

        //저장후 데이터 조회후 리턴
        return categoryQueryRepository.findCategoryOne(categoryRepository.save(categoryEntity).getId()).orElseThrow(() -> new CategoryException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    /**
     * Category 수정
     */
    @Transactional
    public CategoryApiDto modifyCategory(Long id, CategoryDto dto){

        /*수정시 depth는 1이하의 값만 허용*/
        /*1depth 이상은 부모 카테고리 값 수정시 자동 depth 변경*/
        if(!dto.depthValid()){
            throw new CategoryException(ErrorCode.CATEGORY_DEPTH_VALID);
        }

        //수정할 Category 조회
        var findCategoryEntity = categoryRepository.findById(id).orElseThrow(() -> new CategoryException(ErrorCode.CATEGORY_NOT_FOUND));

        //현 부모 객체 저장
        var parentCategoryDto = CategoryDto.dtoConvert(findCategoryEntity.getParentCategory());

        //부모 Category 값이 비어있지 않을시 수정
        if(!dto.parentCategoryisEmpty()){
            //부모 Category 조회
            var pagrentCategoryEntity = categoryRepository.findById(dto.getParentCategory().getId()).orElseThrow(() -> new CategoryException(ErrorCode.CATEGORY_NOT_FOUND));

            //부모 Category 의 (depth + 1)로 수정
            dto.setDepth(pagrentCategoryEntity.getDepth() + 1);

            //같은 depth의 max orderNo에서 1 증가하여 세팅
            dto.setOrderNo(categoryQueryRepository.maxOrderNo(pagrentCategoryEntity.getId(), pagrentCategoryEntity.getDepth() + 1).intValue() + 1);
        }

        //Category 수정후 영향받은 로우 개수 리턴 = 1
        Long updateResult = categoryQueryRepository.updateCategory(id, dto);

        //Category 수정후 수정된 Category 조회
        var updtCompCategory = categoryQueryRepository.findCategoryOne(id).orElseThrow(() -> new CategoryException(ErrorCode.CATEGORY_NOT_FOUND));

        //Category 수정이 정상이고 파라미터로 넘어온 dto에 부모객체가 존재할시
        if(updateResult > 0 && !dto.parentCategoryisEmpty()){
            //orderNo 재정렬 함수 호출
            reOrderNo(parentCategoryDto, findCategoryEntity.getDepth());
            updtDepthChildCategory(Collections.singletonList(updtCompCategory), updtCompCategory.getDepth());
        }
        if(updateResult > 0 && 1 == dto.getDepth()){
            //orderNo 재정렬 함수 호출
            reOrderNo(dto, dto.getDepth());
            updtDepthChildCategory(Collections.singletonList(updtCompCategory), updtCompCategory.getDepth());
            var deleteParent = categoryRepository.findById(id).orElseThrow(() -> new CategoryException(ErrorCode.CATEGORY_NOT_FOUND));
            deleteParent.setParent(null);
        }

        return setChildCategory(Collections.singletonList(updtCompCategory)).stream().findFirst().orElseThrow(() -> new CategoryException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    /**
     * Category 삭제
     * 연관관계 매핑되어 있는 자식 Category 일괄 삭제
     */
    @Transactional
    public SucessResponse deleteCategory(Long id){

        //삭제할 Category 조회
        var findCategoryDto = categoryQueryRepository.findCategoryOne(id).orElseThrow(() -> new CategoryException(ErrorCode.CATEGORY_NOT_FOUND));

        //조회한 Category depth 저장
        int nowDepth = findCategoryDto.getDepth();

        //부모 객체 저장
        var parentCategoryDto = CategoryDto.builder().id(findCategoryDto.getParentCategory().getId()).build();

        //Category 삭제
        try{
            categoryRepository.deleteById(findCategoryDto.getId());
        }catch (Exception e){
            e.printStackTrace();
        }


        //삭제된 Category 조회후 존재하지 않으면 삭제 성공
        if(categoryQueryRepository.findCategoryOne(findCategoryDto.getId()).isEmpty()){
            //orderNo 재정렬 함수 호출
            reOrderNo(parentCategoryDto, nowDepth);
            return new SucessResponse(CategoryEnum.CATEGORY_DELETE_SUCESS.getCode(), CategoryEnum.CATEGORY_DELETE_SUCESS.getMessage());
        }

        return new SucessResponse(CategoryEnum.CATEGORY_DELETE_FAIL.getCode(), CategoryEnum.CATEGORY_DELETE_FAIL.getMessage());
    }

    /**
     * 하위 depth 자식 Category 조회 (재귀함수)
     */
    public List<CategoryApiDto> setChildCategory(List<CategoryApiDto> parentCategoryList) throws CategoryException{

        //파라미터로 넘어온 부모객체의 id 값만 뽑아 자식 Category 조회
        var childResult = categoryQueryRepository.findAllChildCategorys(
                parentCategoryList.stream().map(CategoryApiDto::getId).collect(Collectors.toList())
        );

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

    /**
     * 하위 depth 자식 Category depth 변경 (재귀함수)
     */
    public List<CategoryApiDto> updtDepthChildCategory(List<CategoryApiDto> parentCategoryList, int depth) throws CategoryException{

        //파라미터로 넘어온 부모객체의 id 값만 뽑아 자식 Category 조회
        var childResult = categoryQueryRepository.findAllChildCategorys(
                parentCategoryList.stream().map(CategoryApiDto::getId).collect(Collectors.toList())
        );

        //현 Category에서 자식 Category가 존재하지 않을시 재귀 종료
        if(childResult.isEmpty()){
            return parentCategoryList;
        }

        //부모 Category entity에 자식 Category List 저장
        parentCategoryList = parentCategoryList.stream().map(parentCategoryData -> {
                    var setChildResultList = childResult.stream().filter(childCategoryData -> parentCategoryData.getId() == childCategoryData.getParentCategory().getId()).collect(Collectors.toList());
                    setChildResultList.forEach(
                            setChildResultData -> {
                                categoryQueryRepository.updateCategoryDepth(setChildResultData.getId(), depth + 1);
                            }
                    );
                    parentCategoryData.setChildCategoryList(setChildResultList);
                    return parentCategoryData;
                }
        ).collect(Collectors.toList());

        //재귀 종료 전까지 지속해서 하위 depth 자식 Category 조회
        updtDepthChildCategory(childResult, depth + 1);

        //재귀문 종료시 파라미터로 넘어온 부모 Category 리턴
        return parentCategoryList;
    }

    /**
     *
     * orderNo 재정렬 함수
     * */
    public void reOrderNo(CategoryDto parentCategoryDto, int depth){
        int orderNo = 1;

        List<CategoryApiDto> findAllCategorys = new ArrayList<>();

        //부모 Category가 없을시 현 depth Category 조회
        if(parentCategoryDto.parentCategoryisEmpty()) {
            findAllCategorys = categoryRepository.findByDepthAndDeleteFlag(depth, "N").stream().map(CategoryApiDto::dtoConvert).collect(Collectors.toList());
        }
        //부모 Category id를 이용하여 자식 Category 모두 조회
        else{
            findAllCategorys = categoryQueryRepository.findAllChildCategorys(Collections.singletonList(parentCategoryDto.getId()));
        }

        for (CategoryApiDto tmpDto : findAllCategorys) {
            //자식 Category 루프문 순회하여 orderNo 재정렬
            tmpDto.setOrderNo(orderNo);
            orderNo++;
            categoryQueryRepository.updateCategoryOrderNo(tmpDto.getId(), tmpDto.getOrderNo());
        }
    }

}
