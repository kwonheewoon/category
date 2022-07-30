package report.category.dto;

import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.LastModifiedDate;
import report.category.entity.CategoryEntity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto {

    private long id;

    private String categoryNm;

    private int orderNo;

    private CategoryDto parentCategory;

    private List<CategoryDto> childCategoryList = new ArrayList<>();

    private String deleteFlag;

    private LocalDateTime createDate;

    private LocalDateTime lastModifiedDate;

    public CategoryDto(long id, String categoryNm, int orderNo, String deleteFlag, LocalDateTime createDate, LocalDateTime lastModifiedDate) {
        this.id = id;
        this.categoryNm = categoryNm;
        this.orderNo = orderNo;
        this.deleteFlag = deleteFlag;
        this.createDate = createDate;
        this.lastModifiedDate = lastModifiedDate;
    }

    public static CategoryDto dtoConvert(CategoryEntity categoryEntity){
        return CategoryDto.builder()
                .id(categoryEntity.getId())
                .categoryNm(categoryEntity.getCategoryNm())
                .orderNo(categoryEntity.getOrderNo())
                .createDate(categoryEntity.getCreateDate())
                .lastModifiedDate(categoryEntity.getLastModifiedDate())
                .parentCategory(new CategoryDto(categoryEntity.getId(), categoryEntity.getCategoryNm(), categoryEntity.getOrderNo(), categoryEntity.getDeleteFlag(), categoryEntity.getCreateDate(), categoryEntity.getLastModifiedDate()))
                .childCategoryList(
                        categoryEntity.getChildCategoryList().stream().map(childCategoryData -> {
                            return new CategoryDto(childCategoryData.getId(), childCategoryData.getCategoryNm(), childCategoryData.getOrderNo(), childCategoryData.getDeleteFlag(), childCategoryData.getCreateDate(), childCategoryData.getLastModifiedDate());
                        }).collect(Collectors.toList())
                )
                .build();
    }

}
