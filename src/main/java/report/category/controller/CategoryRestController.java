package report.category.controller;

import lombok.RequiredArgsConstructor;
import nonapi.io.github.classgraph.json.JSONUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import report.category.dto.CategoryDto;
import report.category.service.CategoryService;
import report.category.vo.CategoryVo;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CategoryRestController {
    private final CategoryService categoryService;

    @GetMapping("/categorys")
    public ResponseEntity<Object> categorys(@RequestBody CategoryVo vo){
        JSONObject results = new JSONObject();

        var resultList = categoryService.categoryList(vo);
        results.put("result", resultList);

        return new ResponseEntity<>(results, HttpStatus.OK);
    }

    @PostMapping("/categorys")
    public ResponseEntity<Object> save(@RequestBody CategoryDto dto){

        return new ResponseEntity<>(categoryService.saveCategory(dto), HttpStatus.CREATED);

    }

    @PatchMapping("/categorys/{id}")
    public ResponseEntity<Object> modify(@PathVariable Long id, @RequestBody Map<Object, Object> fields){
        return new ResponseEntity<>(categoryService.modifyCategory(id, fields), HttpStatus.CREATED);

    }

    @DeleteMapping("/categorys/{id}")
    ResponseEntity<Object> delete(@PathVariable Long id){
        return new ResponseEntity<>(categoryService.deleteCategory(id), HttpStatus.CREATED);

    }

}
