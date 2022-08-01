package report.category.enumclass;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CategoryEnum {


    /*
     * 201050 : 카테고리가 정상적으로 삭제 되었습니다.
     */
    CATEGORY_DELETE_SUCESS("201050", "카테고리가 정상적으로 삭제 되었습니다."),

    /*
     * 201051 : 카테고리 삭제에 실패하였습니다.
     */
    CATEGORY_DELETE_FAIL("201051", "카테고리 삭제에 실패하였습니다.");


    private final String code;
    private final String message;

}