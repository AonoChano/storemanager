package com.wjy.storemanager.controller;

import com.wjy.storemanager.common.Result;
import com.wjy.storemanager.entity.Category;
import com.wjy.storemanager.service.CategoryService;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    //查所有
//    @GetMapping("list")
//    List<Category> selectAll(){
//        return categoryService.selectAll();
//    }
    @GetMapping("/list")
    public Result<List<Category>> list() {
        return Result.success(categoryService.selectAll());
    }


    //精准查询
//    @GetMapping("/{id}")
//    public Category selectByPrimaryKey(@PathVariable long id){
//        return categoryService.selectByPrimaryKey(id);
//    }
    @GetMapping("/{id}")
    public Result<Category> selectByPrimaryKey(@PathVariable("id") long id) {
        return Result.success(categoryService.selectByPrimaryKey(id));
    }


    //删
//    @PostMapping("/{id}")
//    public int delete(@PathVariable long id){
//        return categoryService.deleteByPrimaryKey(id);
//    }
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") long id) {
        categoryService.deleteByPrimaryKey(id);
        return Result.success();
    }

    //改
//    @PostMapping("/update")
//    public int updateByPrimaryKey(Category category) {
//        return categoryService.updateByPrimaryKey(category);
//    }
    @PutMapping("/update")
    public Result<Void>update(@RequestBody Category category){
        categoryService.updateByPrimaryKey(category);
        return Result.success();
    }

    //增
//    @PostMapping("/add")
//    public int insert(Category category) {
//        return categoryService.insert(category);
//
//    }
    @PostMapping("/add")
    public Result<Void> add(@RequestBody Category category){
        categoryService.insert(category);
        return Result.success();
    }
}
