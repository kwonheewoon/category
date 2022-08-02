package report.category;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import report.category.controller.CategoryRestController;
import report.category.dto.CategoryApiDto;
import report.category.dto.CategoryDto;
import report.category.enumclass.CategoryEnum;
import report.category.service.CategoryService;
import report.category.vo.CategoryVo;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryRestController.class)
@MockBean(JpaMetamodelMappingContext.class)
public class CategoryRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    CategoryService categoryService;

    @BeforeEach
    void setup(){

    }

    @Test
    @DisplayName("카테고리 전체 조회")
    void 카테고리_전체_조회() throws Exception {
        CategoryVo vo = new CategoryVo();

        given(categoryService.categoryList(any())).willReturn(Arrays.asList(new CategoryApiDto(1L, "카테고리1", 1, "N", LocalDateTime.now(), LocalDateTime.now())));

        String expectByUsername = "$.result[0].categoryNm";

        mockMvc.perform(
                get("/categorys")
                        .content("{}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectByUsername).exists())

                .andDo(print());

        verify(categoryService).categoryList(refEq(vo));
    }

    @Test
    @DisplayName("상위 카테고리 조회")
    void 상위_카테고리_조회() throws Exception {
        CategoryVo vo = new CategoryVo();

        given(categoryService.find(anyLong())).willReturn(new CategoryApiDto(1L, "카테고리1", 1, "N", LocalDateTime.now(), LocalDateTime.now()));

        String expectByUsername = "$.result.categoryNm";

        mockMvc.perform(
                        get("/categorys/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectByUsername).exists())

                .andDo(print());

        verify(categoryService).find(1L);
    }

    @Test
    @DisplayName("카테고리 저장")
    void 카테고리_저장() throws Exception {
        CategoryDto dto = new CategoryDto();
        dto.setCategoryNm("카테고리1 등록");
        dto.setParentCategory(CategoryDto.builder().id(2L).build());

        given(categoryService.saveCategory(any(CategoryDto.class))).willReturn(new CategoryApiDto(1L, "카테고리1 등록", 1, "N", LocalDateTime.now(), LocalDateTime.now()));

        String expectByUsername = "$.categoryNm";

        mockMvc.perform(
                        post("/categorys")
                                .content("{" +
                                        "\"categoryNm\" : \"카테고리1 등록\"," +
                                        "\"parentCategory\" : {" +
                                                    "\"id\":2" +
                                                "}" +
                                        "}")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath(expectByUsername).exists())

                .andDo(print());

        verify(categoryService).saveCategory(refEq(dto));
    }

    @Test
    @DisplayName("카테고리 수정")
    void 카테고리_수정() throws Exception {
        CategoryDto dto = new CategoryDto();
        dto.setCategoryNm("카테고리1 수정");
        dto.setParentCategory(CategoryDto.builder().id(3L).build());

        given(categoryService.modifyCategory(any(),any())).willReturn(new CategoryApiDto(1L, "카테고리1 수정", 1, "N", LocalDateTime.now(), LocalDateTime.now()));

        String expectByUsername = "$.categoryNm";

        mockMvc.perform(
                        patch("/categorys/1")
                                .content("{" +
                                            "\"categoryNm\" : \"카테고리1 수정\"," +
                                            "\"parentCategory\" : {" +
                                            "\"id\":3" +
                                            "}" +
                                        "}")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath(expectByUsername).exists())

                .andDo(print());

        verify(categoryService).modifyCategory(1L, dto);
    }

    @Test
    @DisplayName("카테고리 삭제")
    void 카테고리_삭제() throws Exception {
        CategoryDto dto = new CategoryDto();
        dto.setCategoryNm("카테고리1 수정");
        dto.setParentCategory(CategoryDto.builder().id(3L).build());
        //doNothing().when(categoryService).deleteCategory(any());
        given(categoryService.deleteCategory(anyLong())).willReturn(CategoryEnum.CATEGORY_DELETE_SUCESS.getMessage());

        mockMvc.perform(
                        delete("/categorys/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());

        verify(categoryService).deleteCategory(1L);
    }
}
