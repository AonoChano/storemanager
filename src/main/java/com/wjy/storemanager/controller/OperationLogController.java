package com.wjy.storemanager.controller;

import com.wjy.storemanager.common.Result;
import com.wjy.storemanager.entity.OperationLog;
import com.wjy.storemanager.mapper.OperationLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/operationLog")
public class OperationLogController {
    @Autowired
    private OperationLogMapper operationLogMapper;
    @GetMapping("/list")
    public Result<List<OperationLog>> selectAllOperationLog(){
        return Result.success(operationLogMapper.selectAll());
    }


}
