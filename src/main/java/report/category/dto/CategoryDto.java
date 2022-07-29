package report.category.dto;

import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.LastModifiedDate;
import report.category.entity.CategoryEntity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto {

    private long id;

    private String categoryNm;

    private int orderNo;

    private CategoryEntity parentCategory;

    private List<CategoryEntity> childCategoryList = new ArrayList<>();

    private String deleteFlag;

    private LocalDateTime createDate;

    private LocalDateTime lastModifiedDate;



    public static CategoryDto dtoConvert(CategoryEntity categoryEntity){
        return CategoryDto.builder()
                .categoryNm(categoryEntity.getCategoryNm())
                .orderNo(categoryEntity.getOrderNo())
                .createDate(categoryEntity.getCreateDate())
                .lastModifiedDate(categoryEntity.getLastModifiedDate())
                .build();
    }
}
