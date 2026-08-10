package com.wjy.storemanager.service;

import com.wjy.storemanager.entity.Category;

import java.util.List;

public interface CategoryService {
//查询所有分类
List<Category> selectAll();
//按id查询
Category selectByPrimaryKey(long id);
//增加
int insert(Category category);
//删除
int deleteByPrimaryKey(long id);
//修改
int updateByPrimaryKey(Category category);
}
