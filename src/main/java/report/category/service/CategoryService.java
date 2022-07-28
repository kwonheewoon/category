package report.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import report.category.dto.CategoryDto;
import report.category.repository.CategoryQueryRepository;
import report.category.repository.CategoryRepository;
import report.category.vo.CategoryVo;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryQueryRepository categoryQueryRepository;

    public List<CategoryDto> categoryList(CategoryVo vo, Pageable pageable){
        return categoryQueryRepository.categoryList(vo, pageable);
    }
}
