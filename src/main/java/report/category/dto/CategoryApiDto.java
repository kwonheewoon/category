package report.category.dto;

import lombok.*;
import report.category.entity.CategoryEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryApiDto {

    private long id;

    private String categoryNm;

    private int orderNo;

    private ParentCategoryApiDto parentCategory = new ParentCategoryApiDto();

    private List<CategoryApiDto> childCategoryList = new ArrayList<>();

    private String deleteFlag;

    private LocalDateTime createDate;

    private LocalDateTime lastModifiedDate;

    public CategoryApiDto(long id, String categoryNm, int orderNo, String deleteFlag, LocalDateTime createDate, LocalDateTime lastModifiedDate) {
        this.id = id;
        this.categoryNm = categoryNm;
        this.orderNo = orderNo;
        this.deleteFlag = deleteFlag;
        this.createDate = createDate;
        this.lastModifiedDate = lastModifiedDate;
    }

    public static CategoryApiDto dtoConvert(CategoryEntity categoryEntity){
        return CategoryApiDto.builder()
                .id(categoryEntity.getId())
                .categoryNm(categoryEntity.getCategoryNm())
                .orderNo(categoryEntity.getOrderNo())
                .createDate(categoryEntity.getCreateDate())
                .lastModifiedDate(categoryEntity.getLastModifiedDate())
                /*.parentCategory(
                        new ParentCategoryApiDto(categoryEntity.getParentCategory())
                )*/
                /*.childCategoryList(
                        categoryEntity.getChildCategoryList().stream().map(childCategoryData -> {
                            return new CategoryApiDto(childCategoryData.getId(), childCategoryData.getCategoryNm(), childCategoryData.getOrderNo(), childCategoryData.getDeleteFlag(), childCategoryData.getCreateDate(), childCategoryData.getLastModifiedDate());
                        }).collect(Collectors.toList())
                )*/
                .build();
    }

    @Data
    @NoArgsConstructor
    public static class ParentCategoryApiDto{
        public ParentCategoryApiDto(long id, String categoryNm) {
            this.id = id;
            this.categoryNm = categoryNm;
        }

        private long id;
        private String categoryNm;
    }
}
