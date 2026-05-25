package com.shelfflow.controller.user;

import com.shelfflow.entity.Bundle;
import com.shelfflow.result.Result;
import com.shelfflow.service.BundleService;
import com.shelfflow.vo.ProductItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userBundleController")
@RequestMapping("/user/bundle")
@Slf4j
@Api(tags = "用户端-组合包接口")
public class BundleController {
    @Autowired
    private BundleService bundleService;

    @ApiOperation("根据categoryId查询组合包")
    @GetMapping("/list")
    @Cacheable(cacheNames="bundle", key="#categoryId")
    public Result<List<Bundle>> getListByCategoryId(Long categoryId){
        log.info("根据categoryId查询组合包,categoryId:{}",categoryId);
        List<Bundle> list = bundleService.getListByCategoryId(categoryId);
        return Result.success(list);
    }

    @ApiOperation("根据setemealId查询product")
    @GetMapping("/product/{id}")
    public Result<List<ProductItemVO>> getProductsByBundleId(@PathVariable Long id){
        log.info("根据bundleId查询product,bundleId:{}",id);
        List<ProductItemVO> list = bundleService.getProductsByBundleId(id);
        return Result.success(list);
    }
}
