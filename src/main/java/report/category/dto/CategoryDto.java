package report.category.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import report.category.entity.CategoryEntity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
public class CategoryDto {

    private long id;

    private String categoryNm;

    private int orderNo;

    private CategoryEntity parentCategory;

    private List<CategoryEntity> childCategoryList = new ArrayList<>();

    private String deleteFlag;
}
