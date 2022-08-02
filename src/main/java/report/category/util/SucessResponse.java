package report.category.util;

import lombok.Getter;
import org.json.simple.JSONObject;
import report.category.enumclass.CategoryEnum;
import report.category.exception.ErrorCode;

@Getter
public class SucessResponse <T>{

    //private final LocalDateTime timestamp = LocalDateTime.now();
    //private final int status;
    //private final String error;
    private final String code;
    private final String message;
    private T result;

    public SucessResponse(CategoryEnum categoryEnum, T parameter) {
        //this.status = errorCode.getStatus().value();
        //this.error = errorCode.getStatus().name();
        this.code = categoryEnum.getCode();
        this.message = categoryEnum.getMessage();
        this.result = parameter;
    }

}
