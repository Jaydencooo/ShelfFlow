package com.shelfflow.controller.user;


import com.shelfflow.result.Result;
import com.shelfflow.service.ProductService;
import com.shelfflow.vo.ProductVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userProductController")
@RequestMapping("/user/product")
@Slf4j
@Api(tags="用户端-商品接口")
public class ProductController {
    @Autowired
    ProductService productService;
    @Autowired
    RedisTemplate redisTemplate;

    @ApiOperation("根据categoryId查询product")
    @GetMapping("/list")
    public Result<List<ProductVO>> getListByCategoryId(Long categoryId){
        log.info("根据categoryId查询product, categoryId:{}",categoryId);

        String key = "product_" + categoryId;
        List<ProductVO> list = (List<ProductVO>)redisTemplate.opsForValue().get(key);
        if(list != null && !list.isEmpty()){
            return Result.success(list);
        }

        list = productService.getListByCategoryId(categoryId);
        redisTemplate.opsForValue().set(key, list);
        return Result.success(list);
    }

}
