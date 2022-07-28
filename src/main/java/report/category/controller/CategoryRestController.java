package report.category.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import report.category.dto.CategoryDto;
import report.category.service.CategoryService;
import report.category.vo.CategoryVo;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CategoryRestController {
    private final CategoryService categoryService;

    @GetMapping("/categorys")
    public ResponseEntity<List<CategoryDto>> categorys(@RequestBody CategoryVo vo){
        PageRequest pageRequest = PageRequest.of(vo.getPage(), vo.getSize());
        return ResponseEntity.ok().body(categoryService.categoryList(vo, pageRequest));
    }

}
