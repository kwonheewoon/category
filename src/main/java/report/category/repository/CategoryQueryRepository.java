package report.category.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import report.category.dto.CategoryDto;
import report.category.vo.CategoryVo;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CategoryQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<CategoryDto> categoryList(CategoryVo vo, Pageable pageable){
        return new ArrayList<CategoryDto>();
    }
}
