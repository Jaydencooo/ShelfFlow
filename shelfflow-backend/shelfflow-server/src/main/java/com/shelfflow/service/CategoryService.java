package com.shelfflow.service;

import com.shelfflow.dto.CategoryDTO;
import com.shelfflow.dto.CategoryPageQueryDTO;
import com.shelfflow.entity.Category;
import com.shelfflow.result.PageResult;

import java.util.List;

public interface CategoryService {
    PageResult categoryPageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    void add(CategoryDTO categoryDTO);

    void deleteById(Integer id);

    void update(CategoryDTO categoryDTO);

    void updateStatus(Long id, Integer status);

    List<Category> getByType(Integer type);
}
