package report.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;
import report.category.dto.CategoryDto;
import report.category.entity.CategoryEntity;
import report.category.repository.CategoryQueryRepository;
import report.category.repository.CategoryRepository;
import report.category.vo.CategoryVo;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryQueryRepository categoryQueryRepository;

    public Page<CategoryDto> categoryList(CategoryVo vo){
        var pageInfo = vo.getPaging();
        var pageable = PageRequest.of(pageInfo.getPageNo() - 1, pageInfo.getCountPerPage());
        return categoryQueryRepository.categoryList(vo, pageable);
    }

    public CategoryDto saveCategory(CategoryDto dto){
        return CategoryDto.dtoConvert(
                categoryRepository.save(CategoryEntity
                        .builder()
                        .categoryNm(dto.getCategoryNm())
                        .orderNo(dto.getOrderNo())
                        .deleteFlag(dto.getDeleteFlag())
                        .build())
                );
    }

    @Transactional
    public CategoryDto modifyCategory(Long id, Map<Object, Object> fields){
        var findCategoryEntity = categoryRepository.findById(id);
        if(findCategoryEntity.isPresent()) {
            fields.forEach((key, value) -> {
                Field field = ReflectionUtils.findField(CategoryEntity.class, (String) key);
                field.setAccessible(true);
                ReflectionUtils.setField(field, findCategoryEntity.get(), value);
            });
            var updatedCategoryDto = CategoryDto.dtoConvert(categoryRepository.save(findCategoryEntity.get()));
            return updatedCategoryDto;
        }
        return null;
    }

    @Transactional
    public Long deleteCategory(Long id){
        return categoryQueryRepository.deleteCategory(id);
    }
}
