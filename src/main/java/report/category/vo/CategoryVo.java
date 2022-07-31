package report.category.vo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryVo {

    private String categoryNm;

    private Long searchCategoryId;
}
