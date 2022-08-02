package report.category.dto;

import lombok.*;
import report.category.entity.CategoryEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto {

    /*카테고리 ID*/
    private long id;

    /*카테고리 명*/
    private String categoryNm;

    /*카테고리 정렬 번호*/
    private int orderNo;

    /*카테고리 계층*/
    private int depth;

    /*부모 카테고리*/
    private CategoryDto parentCategory;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoryDto that = (CategoryDto) o;
        return id == that.id && orderNo == that.orderNo && depth == that.depth && Objects.equals(categoryNm, that.categoryNm) && Objects.equals(parentCategory, that.parentCategory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, categoryNm, orderNo, depth, parentCategory);
    }

    public static CategoryDto dtoConvert(CategoryEntity categoryEntity){

        if(null != categoryEntity){
            return CategoryDto.builder()
                    .id(categoryEntity.getId())
                    .categoryNm(categoryEntity.getCategoryNm())
                    .orderNo(categoryEntity.getOrderNo())
                    .depth(categoryEntity.getDepth())
                    .build();
        }
        return new CategoryDto();
    }

}
