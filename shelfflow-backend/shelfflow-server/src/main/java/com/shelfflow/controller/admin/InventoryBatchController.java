package com.shelfflow.controller.admin;

import com.shelfflow.dto.InventoryBatchDTO;
import com.shelfflow.dto.InventoryBatchPageQueryDTO;
import com.shelfflow.result.PageResult;
import com.shelfflow.result.Result;
import com.shelfflow.service.InventoryBatchService;
import com.shelfflow.vo.InventoryBatchRefreshVO;
import com.shelfflow.vo.InventoryBatchVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/inventory-batch")
@Slf4j
public class InventoryBatchController {

    @Autowired
    private InventoryBatchService inventoryBatchService;

    @PostMapping
    @ApiOperation("新增库存批次")
    public Result add(@RequestBody InventoryBatchDTO inventoryBatchDTO) {
        log.info("新增库存批次: {}", inventoryBatchDTO);
        inventoryBatchService.add(inventoryBatchDTO);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("修改库存批次")
    public Result update(@RequestBody InventoryBatchDTO inventoryBatchDTO) {
        log.info("修改库存批次: {}", inventoryBatchDTO);
        inventoryBatchService.update(inventoryBatchDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("库存批次分页查询")
    public Result<PageResult> pageQuery(InventoryBatchPageQueryDTO inventoryBatchPageQueryDTO) {
        log.info("库存批次分页查询: {}", inventoryBatchPageQueryDTO);
        return Result.success(inventoryBatchService.pageQuery(inventoryBatchPageQueryDTO));
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询库存批次")
    public Result<InventoryBatchVO> getById(@PathVariable Long id) {
        return Result.success(inventoryBatchService.getById(id));
    }

    @GetMapping("/product/{productId}")
    @ApiOperation("根据商品id查询库存批次")
    public Result<List<InventoryBatchVO>> listByProductId(@PathVariable Long productId) {
        return Result.success(inventoryBatchService.listByProductId(productId));
    }

    @PostMapping("/status/{status}")
    @ApiOperation("启用或停用库存批次")
    public Result setStatusById(@PathVariable Integer status, Long id) {
        inventoryBatchService.setStatusById(status, id);
        return Result.success();
    }

    @PostMapping("/refresh-status")
    @ApiOperation("刷新库存批次状态")
    public Result<InventoryBatchRefreshVO> refreshStatuses() {
        return Result.success(inventoryBatchService.refreshStatuses());
    }
}
