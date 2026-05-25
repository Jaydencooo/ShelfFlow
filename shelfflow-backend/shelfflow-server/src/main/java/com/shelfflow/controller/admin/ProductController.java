package com.shelfflow.controller.admin;

import com.shelfflow.dto.ProductDTO;
import com.shelfflow.dto.ProductPageQueryDTO;
import com.shelfflow.entity.Product;
import com.shelfflow.result.PageResult;
import com.shelfflow.result.Result;
import com.shelfflow.service.ProductService;
import com.shelfflow.vo.ProductVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController("adminProductController")
@Slf4j
@Api(tags = "商品 ProductController")
@RequestMapping("/admin/product")
public class ProductController {

    @Autowired
    private ProductService productService;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    @ApiOperation("新增商品（包括规格）")
    public Result add(@RequestBody ProductDTO productDTO) {
        log.info("新增商品（包括规格）,productDTO:{}", productDTO);
        productService.add(productDTO);

        //清除redis缓存
        String pattern = "product_" + productDTO.getCategoryId();
        redisDelete(pattern);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("商品分页查询")
    public Result<PageResult> productPageQuery(ProductPageQueryDTO productPageQueryDTO){
        log.info("商品分页查询:{}",productPageQueryDTO);
        PageResult pageResult = productService.productPageQuery(productPageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping()
    @ApiOperation("根据id删除product和productSpec")
    public Result deleteBatch(@RequestParam List<Long> ids){
        log.info("根据id删除product和productSpec,ids:{}",ids);
        productService.deleteBatch(ids);

        String pattern = "product_*";
        redisDelete(pattern);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询product(回显)")
    public Result<ProductVO> getById(@PathVariable Long id){
        log.info("根据id查询product(回显):{}",id);
        ProductVO productVO = productService.getById(id);
        return Result.success(productVO);
    }

    @PutMapping
    @ApiOperation("修改product")
    public Result update(@RequestBody ProductDTO productDTO){
        log.info("修改product:{}",productDTO);
        productService.update(productDTO);

        String pattern = "product_*";
        redisDelete(pattern);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("商品起售停售(根据id改status)")
    public Result setStatusById(@PathVariable Integer status, Long id){
        log.info("商品起售停售(根据id改status),id:{},status:{}",id , status);
        productService.setStatusById(status, id);
        String pattern = "product_*";
        redisDelete(pattern);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("根据categoryId查询product")
    public Result<List<Product>> geteByCategoryId(Long categoryId){
        log.info("根据categoryId查询product,categoryId:{}",categoryId);
        List<Product> list= productService.getByCategoryId(categoryId);
        return Result.success(list);
    }

    //批量删除key
    private void redisDelete(String pattern){
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception exception) {
            log.warn("redis cache clear skipped, pattern={}", pattern, exception);
        }
    }


}
