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
import report.category.repository.CategoryQueryRepository;
import report.category.repository.CategoryRepository;
import report.category.vo.CategoryVo;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryQueryRepository categoryQueryRepository;

    public List<CategoryApiDto> categoryList(CategoryVo vo){
        //var result = categoryRepository.findAllByD(Sort.by(Sort.Direction.DESC, "orderNo")).stream().map(CategoryApiDto::dtoConvert).collect(Collectors.toList());

        //상위 카테고리가 있을시
            var result = categoryQueryRepository.findAllcategorys(vo);
            //조회한 상위 카테고리 ID값 가공하여 LIST 리턴
            setChildCategory(result);

        return result;
    }

    public CategoryApiDto saveCategory(CategoryDto dto){

        //저장할 카테고리 entity 생성
        var categoryEntity = CategoryEntity
                .builder()
                .categoryNm(dto.getCategoryNm())
                .orderNo(dto.getOrderNo())
                .deleteFlag(dto.getDeleteFlag())
                .build();

        //파라미터의 부모 객체가 null이 아닐경우
        if(null != dto.getParentCategory()) {
            //부모 entity 조회
            var findCategoryEntity = categoryRepository.findByIdAndDeleteFlag(dto.getParentCategory().getId(), "N").orElseThrow();
            //부모 entity의 (depth + 1)
            categoryEntity.changeDepth(findCategoryEntity.getDepth() + 1);
            //부모 entity 저장
            categoryEntity.setParent(findCategoryEntity);
        }

        //같은 depth의 max orderNo에서 1 증가하여 세팅
        categoryEntity.changeOrderNo(categoryQueryRepository.maxOrderNo(categoryEntity.getDepth()));

        //저장후 반환된 entity -> dto 변환하여 리턴
        return CategoryApiDto.dtoConvert(
                categoryRepository.save(categoryEntity)
        );
    }

    @Transactional
    public CategoryApiDto modifyCategory(Long id, Map<Object, Object> fields){
        var findCategoryEntity = categoryRepository.findById(id);
        if(findCategoryEntity.isPresent()) {
            fields.forEach((key, value) -> {
                Field field = ReflectionUtils.findField(CategoryEntity.class, (String) key);
                field.setAccessible(true);
                ReflectionUtils.setField(field, findCategoryEntity.get(), value);
            });
            var updatedCategoryDto = CategoryApiDto.dtoConvert(categoryRepository.save(findCategoryEntity.get()));
            return updatedCategoryDto;
        }
        return null;
    }

    @Transactional
    public Long deleteCategory(Long id){
        return categoryQueryRepository.deleteCategory(id);
    }

    public List<CategoryApiDto> setChildCategory(List<CategoryApiDto> parentCategoryList){
        var childResult = categoryQueryRepository.findAllChcildCategorys(parentCategoryList.stream().map(CategoryApiDto::getId).collect(Collectors.toList()));

        if(childResult.isEmpty()){
            return parentCategoryList;
        }

        //부모 category entity에 자식 category list 저장
        parentCategoryList.stream().map(parentCategoryData -> {
                    parentCategoryData.setChildCategoryList(childResult);
                    return parentCategoryData;
                }
        ).collect(Collectors.toList());

        setChildCategory(childResult);

        return parentCategoryList;
    }
}
