package com.wjy.storemanager.controller;

import com.wjy.storemanager.common.Result;
import com.wjy.storemanager.entity.Supplier;
import com.wjy.storemanager.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/supplier")
public class SupplierController {
    @Autowired
    private SupplierService supplierService;
    //增
    @PostMapping("/add")
    public Result<Void> add(@RequestBody Supplier supplier){
        supplierService.insert(supplier);
        return Result.success();
    }
    //删
    @DeleteMapping("/{id}")
    public Result<Void>delete(@PathVariable("id") long id){
        supplierService.delete(id);
        return Result.success();
    }
    //改
    @PutMapping("/update")
    public Result<Void> update(@RequestBody Supplier supplier){
        supplierService.updateByPrimaryKey(supplier);
        return Result.success();

    }
    //查(根据id)
    @GetMapping("/{id}")
    public Result<Supplier>selectById(@PathVariable("id") long id ){
        return Result.success(supplierService.selectByPrimaryKey(id));
    }
    //查所有
    @GetMapping("/list")
    public Result<List<Supplier>> selectAll(){
        return Result.success(supplierService.selectAll());
    }

}
