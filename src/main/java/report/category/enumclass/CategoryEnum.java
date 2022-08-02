package report.category.enumclass;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CategoryEnum {

    /*
     * 200010 : 카테고리가 정상적으로 조회 되었습니다.
     */
    CATEGORY_FIND_SUCESS("200010", "카테고리가 정상적으로 조회 되었습니다."),

    /*
     * 201010 : 카테고리가 정상적으로 삭제 되었습니다.
     */
    CATEGORY_SAVE_SUCESS("201010", "카테고리가 정상적으로 등록 되었습니다."),

    /*
     * 201011 : 카테고리가 정상적으로 수정 되었습니다.
     */
    CATEGORY_MODIFY_SUCESS("201011", "카테고리가 정상적으로 수정 되었습니다."),

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