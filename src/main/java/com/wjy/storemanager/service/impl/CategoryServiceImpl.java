package com.wjy.storemanager.service.impl;

import com.wjy.storemanager.entity.Category;
import com.wjy.storemanager.mapper.CategoryMapper;
import com.wjy.storemanager.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;
    @Override
    public List<Category> selectAll() {
        return categoryMapper.selectAll();
    }

    @Override
    public Category selectByPrimaryKey(long id) {
        return categoryMapper.selectByPrimaryKey(id);
    }

    @Override
    public int insert(Category category) {
        category.setCreateTime(new Date());
        return categoryMapper.insert(category);
    }

    @Override
    public int deleteByPrimaryKey(long id) {
        return categoryMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKey(Category category) {
        return categoryMapper.updateByPrimaryKey(category);
    }
}
