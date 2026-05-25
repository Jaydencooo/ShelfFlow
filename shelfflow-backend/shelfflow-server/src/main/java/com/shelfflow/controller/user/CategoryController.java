package com.shelfflow.controller.user;


import com.shelfflow.entity.Category;
import com.shelfflow.result.Result;
import com.shelfflow.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userCategoryController")
@RequestMapping("/user/category")
@Slf4j
@Api(tags = "用户端-种类接口")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/list")
    @ApiOperation("根据类型id查询分类的list")
    public Result<List<Category>> getListByType(Integer type){
        log.info("根据类型查询分类list,type:{}",type);
        List<Category> list = categoryService.getByType(type);
        return Result.success(list);

    }
}
