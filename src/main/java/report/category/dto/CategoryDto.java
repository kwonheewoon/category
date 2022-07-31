package report.category.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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



}
