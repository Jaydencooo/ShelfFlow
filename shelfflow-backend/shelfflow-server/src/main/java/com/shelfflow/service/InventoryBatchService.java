package com.shelfflow.service;

import com.shelfflow.dto.InventoryBatchDTO;
import com.shelfflow.dto.InventoryBatchPageQueryDTO;
import com.shelfflow.result.PageResult;
import com.shelfflow.vo.InventoryBatchVO;
import com.shelfflow.vo.InventoryBatchRefreshVO;

import java.util.List;

public interface InventoryBatchService {

    void add(InventoryBatchDTO inventoryBatchDTO);

    void update(InventoryBatchDTO inventoryBatchDTO);

    PageResult pageQuery(InventoryBatchPageQueryDTO inventoryBatchPageQueryDTO);

    InventoryBatchVO getById(Long id);

    List<InventoryBatchVO> listByProductId(Long productId);

    void setStatusById(Integer status, Long id);

    InventoryBatchRefreshVO refreshStatuses();
}
