package com.shelfflow.services.admin.inventorybatch.persistence;

import com.shelfflow.services.admin.inventorybatch.persistence.dataobject.InventoryBatchPageCriteria;
import com.shelfflow.services.admin.inventorybatch.persistence.dataobject.InventoryBatchDataObject;
import com.shelfflow.services.admin.inventorybatch.persistence.dataobject.InventoryBatchPageRow;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.time.LocalDateTime;

public interface InventoryBatchPersistenceMapper {

    List<InventoryBatchPageRow> page(@Param("criteria") InventoryBatchPageCriteria criteria);

    long count(@Param("criteria") InventoryBatchPageCriteria criteria);

    InventoryBatchPageRow findById(@Param("id") Long id);

    InventoryBatchDataObject findDataById(@Param("id") Long id);

    Long findIdByBatchCode(@Param("batchCode") String batchCode);

    boolean existsProduct(@Param("productId") Long productId);

    int insert(InventoryBatchDataObject batch);

    int update(InventoryBatchDataObject batch);

    int updateStatus(@Param("id") Long id,
                     @Param("status") Integer status,
                     @Param("updateUser") Long updateUser,
                     @Param("updateTime") LocalDateTime updateTime);

    int deleteById(@Param("id") Long id);

    int restoreSaleableBatches();

    int markExpiredBatches();

    int markSoldOutBatches();
}
