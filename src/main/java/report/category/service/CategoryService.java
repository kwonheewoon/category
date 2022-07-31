package report.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;
import report.category.dto.CategoryApiDto;
import report.category.dto.CategoryDto;
import report.category.entity.CategoryEntity;
import report.category.exception.CategoryException;
import report.category.exception.ErrorCode;
import report.category.repository.CategoryQueryRepository;
import report.category.repository.CategoryRepository;
import report.category.vo.CategoryVo;

import javax.persistence.EntityNotFoundException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryQueryRepository categoryQueryRepository;

    /*
    * 전체 category 데이터 조회
    *   ㄴ하위 category 데이터 조회
    * */
    public List<CategoryApiDto> categoryList(CategoryVo vo) throws CategoryException{
        //var result = categoryRepository.findAllByD(Sort.by(Sort.Direction.DESC, "orderNo")).stream().map(CategoryApiDto::dtoConvert).collect(Collectors.toList());
        //전체 category 데이터 조회
        var result = categoryQueryRepository.findAllcategorys(vo);
        
        //하위 depth category data 조회하여 세팅해준 후 리턴
        return setChildCategory(result);
    }

    /*
     * 상세 category 데이터 조회
     *   ㄴ하위 cateogry 데이터 조회
     * */
    public List<CategoryApiDto> find(Long searchCategoryId) throws CategoryException{
        //var result = categoryRepository.findAllByD(Sort.by(Sort.Direction.DESC, "orderNo")).stream().map(CategoryApiDto::dtoConvert).collect(Collectors.toList());
        //전체 category 데이터 조회
        var result = categoryQueryRepository.findCategoryOne(searchCategoryId);
        
        //하위 depth category data 조회하여 세팅해준 후 리턴
        //메모리 절약을 위해 싱글톤 arrayList 생성
        return setChildCategory(Collections.singletonList(result));
    }

    /*
    * category 데이터 저장
    * */
    public CategoryApiDto saveCategory(CategoryDto dto){

        //저장할 카테고리 entity 생성
        var categoryEntity = CategoryEntity
                .builder()
                .categoryNm(dto.getCategoryNm())
                .orderNo(dto.getOrderNo())
                .deleteFlag(dto.getDeleteFlag())
                .depth(1)
                .build();

        //부모 카테고리가 존재할시 해당 부모 카테고리 하위에서  orderNo 증가를 위한
        //부모 카테고리 id 저장 변수
        long parentCategoryId = 0;

        //파라미터의 부모 객체가 null이 아닐경우
        if(null != dto.getParentCategory()) {
            //부모 entity 조회
            var findCategoryEntity = categoryRepository.findByIdAndDeleteFlag(dto.getParentCategory().getId(), "N").orElseThrow(() -> new CategoryException(ErrorCode.CATEGORY_NOT_FOUND));
            //부모 entity id 저장
            parentCategoryId = findCategoryEntity.getId();
            //부모 entity의 (depth + 1)
            categoryEntity.changeDepth(findCategoryEntity.getDepth() + 1);
            //부모 entity 저장
            categoryEntity.setParent(findCategoryEntity);
        }

        //같은 depth의 max orderNo에서 1 증가하여 세팅
        categoryEntity.changeOrderNo(categoryQueryRepository.maxOrderNo(parentCategoryId, categoryEntity.getDepth()).intValue() + 1);

        //저장후 반환된 entity -> dto 변환하여 리턴
        return CategoryApiDto.dtoConvert(
                categoryRepository.save(categoryEntity)
        );
    }

    /*
    * category 데이터 수정
    * */
    @Transactional
    public Long modifyCategory(Long id, CategoryDto dto){

        //수정할 category data 조회
        var findCategoryEntity = categoryRepository.findById(id).orElseThrow(() -> new CategoryException(ErrorCode.CATEGORY_NOT_FOUND));

        //1.categoryNm 수정
        //findCategoryEntity.changeCategoryNm(dto.getCategoryNm());

        //부모 카테고리 값이 비어있지 않을시 수정
        if(null !=  dto.getParentCategory()){
            //부모 category entity 조회
            var pagrentCategoryEntity = categoryRepository.findById(dto.getParentCategory().getId()).orElseThrow(() -> new CategoryException(ErrorCode.CATEGORY_NOT_FOUND));

            //parent category entity 세팅
            //findCategoryEntity.changeParentCategory(pagrentCategoryEntity);

            //부모 entity의 (depth + 1)로 수정
            //findCategoryEntity.changeDepth(pagrentCategoryEntity.getDepth() + 1);
            dto.setDepth(pagrentCategoryEntity.getDepth() + 1);

            //같은 depth의 max orderNo에서 1 증가하여 세팅
            //findCategoryEntity.changeOrderNo(categoryQueryRepository.maxOrderNo(parentCategoryId, findCategoryEntity.getDepth()).intValue() + 1);
            dto.setOrderNo(categoryQueryRepository.maxOrderNo(pagrentCategoryEntity.getId(), pagrentCategoryEntity.getDepth() + 1).intValue() + 1);
        }

        Long updateResult = categoryQueryRepository.updateCategory(id, dto);

        if(updateResult > 0 && null != dto.getParentCategory()){
            int orderNo = 1;
            for (CategoryApiDto tmpDto : categoryQueryRepository.findAllChcildCategorys(Collections.singletonList(findCategoryEntity.getParentCategory().getId()))) {
                tmpDto.setOrderNo(orderNo);
                orderNo++;
                categoryQueryRepository.updateCategoryOrderNo(tmpDto.getId(), tmpDto.getOrderNo());
            }
        }

        return updateResult;
    }

    /*
    * category 데이터 삭제
    * 연관관계 매핑되어 있는 자식 데이터도 일괄 삭제
    * */
    @Transactional
    public void deleteCategory(Long id){
        var findCategoryEntity = categoryRepository.findById(id).orElseThrow(() -> new CategoryException(ErrorCode.CATEGORY_NOT_FOUND));
        categoryRepository.deleteById(findCategoryEntity.getId());
    }

    /*
    * 하위 depth 자식 category 데이터 조회 (재귀함수)
    * */
    public List<CategoryApiDto> setChildCategory(List<CategoryApiDto> parentCategoryList) throws CategoryException{

        //파라미터로 넘어온 부모객체의 id 값만 뽑아 자식 category 데이터 조회
        var childResult = categoryQueryRepository.findAllChcildCategorys(parentCategoryList.stream().map(CategoryApiDto::getId).collect(Collectors.toList()));

        //현 자식 category 데이터 존재하지 않을시 재귀 종료
        if(childResult.isEmpty()){
            return parentCategoryList;
        }

        //부모 category entity에 자식 category list 저장
        parentCategoryList = parentCategoryList.stream().map(parentCategoryData -> {
                    parentCategoryData.setChildCategoryList(childResult.stream().filter(childCategoryData -> parentCategoryData.getId() == childCategoryData.getParentCategory().getId()).collect(Collectors.toList()));
                    return parentCategoryData;
                }
        ).collect(Collectors.toList());

        //재귀 종료 전까지 지속해서 하위 depth 자식 category 데이터 조회
        setChildCategory(childResult);

        //재귀문 종료시 파라미터로 넘어온 부모 category 데이터 리턴
        return parentCategoryList;
    }
}
