package com.shelfflow.controller.user;

import com.shelfflow.entity.PickupContact;
import com.shelfflow.result.Result;
import com.shelfflow.service.PickupContactService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/pickup-contact")
@Slf4j
@Api(tags="用户端-自提联系人接口")
public class PickupContactController {
    @Autowired
    private PickupContactService pickupContactService;


    @PostMapping
    @ApiOperation("新增自提联系人")
    public Result add(@RequestBody PickupContact pickupContact){
        log.info("新增自提联系人,pickupContact:{}",pickupContact);
        pickupContactService.add(pickupContact);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("查询本用户的所有自提联系人")
    public Result<List<PickupContact>> list(){
        log.info("查询本用户的所有自提联系人");
        return Result.success(pickupContactService.list());
    }

    @GetMapping("/default")
    @ApiOperation("查询本用户的默认自提联系人")
    public Result<List<PickupContact>> defaultList(){
        log.info("查询本用户的默认自提联系人");
        return Result.success(pickupContactService.defaultList());
    }

    @PutMapping("/default")
    @ApiOperation("根据id设置默认自提联系人")
    public Result setDefaultById(@RequestBody PickupContact pickupContact){
        log.info("根据id设置默认自提联系人,id:{}",pickupContact.getId());
        pickupContactService.setDefaultById(pickupContact);
        return Result.success();
    }
    @GetMapping("/{id}")
    @ApiOperation("根据id查询自提联系人")
    public Result<PickupContact> getById(@PathVariable Long id){
        log.info("根据id查询自提联系人,id:{}",id);
        return Result.success(pickupContactService.getById(id));
    }

    @PutMapping()
    @ApiOperation("修改自提联系人")
    public Result update(@RequestBody PickupContact pickupContact){
        log.info("修改自提联系人,pickupContact:{}",pickupContact);
        pickupContactService.update(pickupContact);
        return Result.success();
    }

    @DeleteMapping()
    @ApiOperation("根据id删除自提联系人")
    public Result deleteById(Long id){
        log.info("根据id删除自提联系人,id:{}",id);
        pickupContactService.deleteById(id);
        return Result.success();
    }



}
