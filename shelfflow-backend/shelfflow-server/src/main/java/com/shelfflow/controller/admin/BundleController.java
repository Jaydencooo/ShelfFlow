package com.shelfflow.controller.admin;


import com.shelfflow.dto.BundleDTO;
import com.shelfflow.dto.BundlePageQueryDTO;
import com.shelfflow.result.PageResult;
import com.shelfflow.result.Result;
import com.shelfflow.service.BundleService;
import com.shelfflow.vo.BundleVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("adminBundleController")
@RequestMapping("/admin/bundle")
@Slf4j
@Api(tags = "组合包 BundleController")
public class BundleController {
    @Autowired
    private BundleService bundleService;

    @GetMapping("/page")
    @ApiOperation("组合包分页查询")
    public Result<PageResult> bundlePageQuery(BundlePageQueryDTO bundlePageQueryDTO){
        log.info("组合包分页查询：{}",bundlePageQueryDTO);
        PageResult pageResult = bundleService.bundlePageQuery(bundlePageQueryDTO);
        return Result.success(pageResult);
    }

    @PostMapping()
    @ApiOperation("新增组合包(和bundle-product表)")
    @CacheEvict(cacheNames = "bundle" , key="bundleDTO.categoryId")
    public Result add(@RequestBody BundleDTO bundleDTO){
        log.info("新增组合包(和bundle-product表),bundleDTO:{}",bundleDTO);
        bundleService.add(bundleDTO);
        return Result.success();
    }

    @DeleteMapping
    @ApiOperation("根据ids批量删除bundle")
    @CacheEvict(cacheNames="bundle" , allEntries = true)
    public Result deleteBatch(@RequestParam List<Long> ids){
        log.info("根据ids批量删除bundle,ids:{}",ids);
        bundleService.deleteBatch(ids);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询bundle（回显）")
    public Result<BundleVO> getById(@PathVariable Long id){
        log.info("根据id查询bundle（回显）,id:{}",id);
        BundleVO bundleVO = bundleService.getById(id);
        return Result.success(bundleVO);
    }

    @PutMapping()
    @ApiOperation("修改bundle和bundle_product")
    @CacheEvict(cacheNames="bundle", allEntries = true)
    public Result update(@RequestBody BundleDTO bundleDTO){
        log.info("修改bundle和bundle_product,bundleDTO:{}",bundleDTO);
        bundleService.update(bundleDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("起售停售(根据id修改status)")
    @CacheEvict(cacheNames="bundle", allEntries=true)
    public Result setStatusById(@PathVariable Integer status, Long id){
        log.info("起售停售(根据id修改status),id:{},status:{}",id,status);
        bundleService.setStatusById(status,id);
        return Result.success();
    }

}
