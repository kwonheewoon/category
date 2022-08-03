package report.category.controller;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import report.category.dto.CategoryDto;
import report.category.enumclass.CategoryEnum;
import report.category.exception.CategoryException;
import report.category.service.CategoryService;
import report.category.util.SucessResponse;
import report.category.vo.CategoryVo;


@RestController
@RequiredArgsConstructor
public class CategoryRestController {
    private final CategoryService categoryService;

    /*
    * 전체 Category 조회
    * */
    @GetMapping("/categorys")
    @ApiOperation(value = "1.전체 Category 조회")
    public ResponseEntity<Object> categorys(@RequestBody CategoryVo vo){
        JSONObject results = new JSONObject();

        var resultList = categoryService.categoryList(vo);
        results.put("result", resultList);

        return new ResponseEntity<>(new SucessResponse(CategoryEnum.CATEGORY_FIND_SUCESS, resultList), HttpStatus.OK);
    }

    /*
     * 상위 Category -> 하위 Category 조회
     * */
    @GetMapping("/categorys/{id}")
    @ApiOperation(value = "2.상위 Category 조회")
    public ResponseEntity<Object> find(@PathVariable Long id) throws CategoryException {
        JSONObject results = new JSONObject();

        var resultList = categoryService.find(id);
        results.put("result", resultList);

        return new ResponseEntity<>(new SucessResponse(CategoryEnum.CATEGORY_FIND_SUCESS, resultList), HttpStatus.OK);
    }

    /*
     * Category 저장
     * */
    @PostMapping("/categorys")
    @ApiOperation(value = "3.Category 저장")
    public ResponseEntity<Object> save(@RequestBody CategoryDto dto){

        return new ResponseEntity<>(new SucessResponse(CategoryEnum.CATEGORY_SAVE_SUCESS, categoryService.saveCategory(dto)), HttpStatus.CREATED);

    }

    /*
     * Category 수정
     * */
    @PatchMapping("/categorys/{id}")
    @ApiOperation(value = "4.Category 수정")
    public ResponseEntity<Object> modify(@PathVariable Long id, @RequestBody CategoryDto dto){
        return new ResponseEntity<>(new SucessResponse(CategoryEnum.CATEGORY_MODIFY_SUCESS, categoryService.modifyCategory(id, dto)), HttpStatus.CREATED);

    }

    /*
     * Category 삭제
     * */
    @DeleteMapping("/categorys/{id}")
    @ApiOperation(value = "5.Category 삭제")
    ResponseEntity<Object> delete(@PathVariable Long id){
        return new ResponseEntity<>(categoryService.deleteCategory(id), HttpStatus.CREATED);
    }

}
