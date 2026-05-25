package com.shelfflow.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.shelfflow.context.CurrentActorContext;
import com.shelfflow.dto.CategoryDTO;
import com.shelfflow.dto.CategoryPageQueryDTO;
import com.shelfflow.entity.Category;
import com.shelfflow.mapper.CategoryMapper;
import com.shelfflow.result.PageResult;
import com.shelfflow.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    CategoryMapper categoryMapper;

    @Override
    public PageResult categoryPageQuery(CategoryPageQueryDTO categoryPageQueryDTO){
        PageHelper.startPage(categoryPageQueryDTO.getPage(),categoryPageQueryDTO.getPageSize());
        Page<Category> page = categoryMapper.categoryPageQuery(categoryPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    public void add(CategoryDTO categoryDTO){
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO,category);
//        category.setCreateTime(LocalDateTime.now());
//        category.setUpdateTime(LocalDateTime.now());
//        category.setCreateUser(CurrentActorContext.getCurrentId());
//        category.setUpdateUser(CurrentActorContext.getCurrentId());
        category.setStatus(1);

        categoryMapper.add(category);
    }

    @Override
    public void deleteById(Integer id){
        categoryMapper.deleteById(id);
    }

    @Override
    public void update(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO,category);
//        category.setUpdateTime(LocalDateTime.now());
//        category.setUpdateUser(CurrentActorContext.getCurrentId());

        categoryMapper.update(category);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Category category = Category.builder().id(id).status(status).build();
        categoryMapper.update(category);

    }

    @Override
    public List<Category> getByType(Integer type) {
        List<Category> list = categoryMapper.getByType(type);
        return list;
    }


}
