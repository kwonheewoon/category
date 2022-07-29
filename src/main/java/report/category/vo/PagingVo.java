package report.category.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class PagingVo implements Serializable {

    private static final long serialVersionUID = -7614850214269900462L;

    private int pageNo = 1; // 현재 페이지 번호

    private int countPerPage = 20; // 페이지당 보여줄 목록 개수

    public int getStartNo() {
        return (pageNo - 1) * countPerPage;
    }

    public PagingVo() {
    }

    public PagingVo(int pageNo, int countPerPage) {
        this.pageNo = pageNo;
        this.countPerPage = countPerPage;
    }

}