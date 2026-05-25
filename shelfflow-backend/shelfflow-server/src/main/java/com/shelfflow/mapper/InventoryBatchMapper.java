package com.shelfflow.mapper;

import com.github.pagehelper.Page;
import com.shelfflow.anno.AutoFill;
import com.shelfflow.dto.InventoryBatchPageQueryDTO;
import com.shelfflow.entity.InventoryBatch;
import com.shelfflow.enumeration.OperationType;
import com.shelfflow.vo.InventoryBatchVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface InventoryBatchMapper {

    @AutoFill(OperationType.INSERT)
    void insert(InventoryBatch inventoryBatch);

    @AutoFill(OperationType.UPDATE)
    void update(InventoryBatch inventoryBatch);

    Page<InventoryBatchVO> pageQuery(InventoryBatchPageQueryDTO inventoryBatchPageQueryDTO);

    InventoryBatchVO getById(Long id);

    List<InventoryBatchVO> listByProductId(Long productId);

    int lockStock(@Param("batchId") Long batchId, @Param("quantity") Integer quantity);

    int releaseLockedStock(@Param("batchId") Long batchId, @Param("quantity") Integer quantity);

    int consumeLockedStock(@Param("batchId") Long batchId, @Param("quantity") Integer quantity);

    int markExpiredBatches();

    int markSoldOutBatches();

    int restoreSaleableBatches();
}
