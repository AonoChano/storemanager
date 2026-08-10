package com.wjy.storemanager.controller;

import com.wjy.storemanager.common.Result;
import com.wjy.storemanager.entity.Product;
import com.wjy.storemanager.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {
    @Autowired
    private ProductService productService;
    //增
    @PostMapping("/add")
    public Result<Void>add(@RequestBody Product product){
        productService.insert(product);
        return Result.success();
    }
    //删
    @DeleteMapping("/{id}")
    public Result<Void>delete(@PathVariable("id")long id){
        productService.delete(id);
        return Result.success();
    }
    //改
    @PutMapping("/update")
    public Result<Void>update(@RequestBody Product product){
        productService.update(product);
        return Result.success();
    }
    //查
    @GetMapping("/{id}")
    public Result<Product>select(@PathVariable("id") long id){
        return Result.success(productService.select(id));
    }
    //查所有
    @GetMapping("/list")
    public Result<List<Product>>selectAll(@RequestParam(required = false) String name,
                                          @RequestParam(required = false) Long categoryId,
                                          @RequestParam(required = false) String barcode){
        return Result.success(productService.selectAll(name,categoryId,barcode));
    }

}
