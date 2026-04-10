package edu.university.researchfundsystem.controller;

import edu.university.researchfundsystem.common.Result;
import edu.university.researchfundsystem.entity.FundCategory;
import edu.university.researchfundsystem.service.FundCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class FundCategoryController {

    private final FundCategoryService categoryService;

    @GetMapping("/list")
    public Result<List<FundCategory>> list() {
        return Result.success(categoryService.list());
    }

    @PostMapping("/create")
    public Result<Long> save(@RequestBody FundCategory category) {
        boolean success = categoryService.save(category);
        if (success) {
            return Result.success(category.getId()); // 返回新创建分类的ID
        } else {
            return Result.error("分类创建失败");
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(categoryService.removeById(id));
    }
}