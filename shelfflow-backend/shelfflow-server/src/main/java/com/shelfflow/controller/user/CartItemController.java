package com.shelfflow.controller.user;

import com.shelfflow.dto.CartItemDTO;
import com.shelfflow.entity.CartItem;
import com.shelfflow.mapper.CartItemMapper;
import com.shelfflow.result.Result;
import com.shelfflow.service.CartItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/cartItem")
@Slf4j
@Api(tags="用户端-选品车接口")
public class CartItemController {
    @Autowired
    private CartItemService cartItemService;
    @Autowired
    private CartItemMapper cartItemMapper;

    @ApiOperation("选品车项+1")
    @PostMapping("/add")
    public Result add(@RequestBody CartItemDTO cartItemDTO){
        log.info("选品车项+1,cartItemDTO:{}",cartItemDTO);
        cartItemService.add(cartItemDTO);
        return Result.success();
    }

    @ApiOperation("查询选品车内容")
    @GetMapping("/list")
    public Result<List<CartItem>> list(){
        log.info("查询选品车内容");
        return Result.success(cartItemService.list());
    }

    @ApiOperation("清空选品车")
    @DeleteMapping("/clean")
    public Result clean(){
        log.info("清空选品车");
        cartItemService.clean();
        return Result.success();
    }

    @ApiOperation("选品车项-1")
    @PostMapping("/sub")
    public Result sub(@RequestBody CartItemDTO cartItemDTO){
        log.info("选品车项-1,cartItemDTO:{}",cartItemDTO);
        cartItemService.sub(cartItemDTO);
        return Result.success();
    }

}
