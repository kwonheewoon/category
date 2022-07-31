package report.category.dto;

import lombok.*;
import report.category.entity.CategoryEntity;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryApiDto {

    /*카테고리 ID*/
    private long id;

    /*카테고리 명*/
    private String categoryNm;

    /*카테고리 정렬 번호*/
    private int orderNo;

    /*카테고리 계층*/
    private int depth;

    /*부모 카테고리*/
    private ParentCategoryApiDto parentCategory = new ParentCategoryApiDto();

    /*자식 카테고리*/
    private List<CategoryApiDto> childCategoryList = new ArrayList<>();

    /*삭제 여부*/
    private String deleteFlag;

    /*등록 날짜*/
    private LocalDateTime createDate;

    /*마지막 수정 날짜*/
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
                .depth(categoryEntity.getDepth())
                .createDate(categoryEntity.getCreateDate())
                .lastModifiedDate(categoryEntity.getLastModifiedDate())
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
