package com.shelfflow.controller.admin;

import com.shelfflow.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminPickupPointController")
@RequestMapping("/admin/pickup-point")
@Slf4j
@Api(tags = "管理端-履约点接口")
public class PickupPointController {
    public static final String KEY = "pickup_point_status";
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @PutMapping("/{status}")
    @ApiOperation("设置履约点状态")
    public Result setStatus(@PathVariable Integer status){
        log.info("设置履约点状态status为：{}",status == 1?"可服务":"已暂停");
        redisTemplate.opsForValue().set(KEY, status);
        return Result.success();
    }

    @GetMapping("/status")
    @ApiOperation("获取履约点状态")
    public Result<Integer> getStatus(){
        Integer status = (Integer)redisTemplate.opsForValue().get(KEY);
        if(status == null){
            return Result.error("履约点状态status尚不存在，请联系程序员");
        }
        log.info("获取履约点状态为：{}", status == 1?"可服务":"已暂停");
        return Result.success(status);
    }


}
