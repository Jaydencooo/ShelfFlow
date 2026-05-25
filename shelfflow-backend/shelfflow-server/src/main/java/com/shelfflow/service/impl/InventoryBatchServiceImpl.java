package com.shelfflow.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.shelfflow.dto.InventoryBatchDTO;
import com.shelfflow.dto.InventoryBatchPageQueryDTO;
import com.shelfflow.entity.InventoryBatch;
import com.shelfflow.mapper.InventoryBatchMapper;
import com.shelfflow.result.PageResult;
import com.shelfflow.service.InventoryBatchService;
import com.shelfflow.service.PricingRuleService;
import com.shelfflow.vo.InventoryBatchRefreshVO;
import com.shelfflow.vo.InventoryBatchVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryBatchServiceImpl implements InventoryBatchService {

    @Autowired
    private InventoryBatchMapper inventoryBatchMapper;
    @Autowired
    private PricingRuleService pricingRuleService;

    @Override
    public void add(InventoryBatchDTO inventoryBatchDTO) {
        InventoryBatch inventoryBatch = new InventoryBatch();
        BeanUtils.copyProperties(inventoryBatchDTO, inventoryBatch);
        fillDefaultQuantity(inventoryBatch);
        inventoryBatchMapper.insert(inventoryBatch);
    }

    @Override
    public void update(InventoryBatchDTO inventoryBatchDTO) {
        InventoryBatch inventoryBatch = new InventoryBatch();
        BeanUtils.copyProperties(inventoryBatchDTO, inventoryBatch);
        fillDefaultQuantity(inventoryBatch);
        inventoryBatchMapper.update(inventoryBatch);
    }

    @Override
    public PageResult pageQuery(InventoryBatchPageQueryDTO inventoryBatchPageQueryDTO) {
        refreshStatuses();
        PageHelper.startPage(inventoryBatchPageQueryDTO.getPage(), inventoryBatchPageQueryDTO.getPageSize());
        Page<InventoryBatchVO> page = inventoryBatchMapper.pageQuery(inventoryBatchPageQueryDTO);
        page.getResult().forEach(this::fillRuntimeFields);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public InventoryBatchVO getById(Long id) {
        refreshStatuses();
        InventoryBatchVO inventoryBatchVO = inventoryBatchMapper.getById(id);
        fillRuntimeFields(inventoryBatchVO);
        return inventoryBatchVO;
    }

    @Override
    public List<InventoryBatchVO> listByProductId(Long productId) {
        refreshStatuses();
        return inventoryBatchMapper.listByProductId(productId).stream().map(inventoryBatchVO -> {
            fillRuntimeFields(inventoryBatchVO);
            return inventoryBatchVO;
        }).collect(Collectors.toList());
    }

    @Override
    public void setStatusById(Integer status, Long id) {
        InventoryBatch inventoryBatch = InventoryBatch.builder().id(id).status(status).build();
        inventoryBatchMapper.update(inventoryBatch);
    }

    @Override
    public InventoryBatchRefreshVO refreshStatuses() {
        int saleableCount = inventoryBatchMapper.restoreSaleableBatches();
        int expiredCount = inventoryBatchMapper.markExpiredBatches();
        int soldOutCount = inventoryBatchMapper.markSoldOutBatches();
        return InventoryBatchRefreshVO.builder()
                .saleableCount(saleableCount)
                .expiredCount(expiredCount)
                .soldOutCount(soldOutCount)
                .build();
    }

    private void fillDefaultQuantity(InventoryBatch inventoryBatch) {
        if (inventoryBatch.getLockedQuantity() == null) {
            inventoryBatch.setLockedQuantity(0);
        }
        if (inventoryBatch.getSoldQuantity() == null) {
            inventoryBatch.setSoldQuantity(0);
        }
        if (inventoryBatch.getStatus() == null) {
            inventoryBatch.setStatus(1);
        }
    }

    private void fillRuntimeFields(InventoryBatchVO inventoryBatchVO) {
        int stockQuantity = inventoryBatchVO.getStockQuantity() == null ? 0 : inventoryBatchVO.getStockQuantity();
        int lockedQuantity = inventoryBatchVO.getLockedQuantity() == null ? 0 : inventoryBatchVO.getLockedQuantity();
        int soldQuantity = inventoryBatchVO.getSoldQuantity() == null ? 0 : inventoryBatchVO.getSoldQuantity();
        inventoryBatchVO.setAvailableQuantity(stockQuantity - lockedQuantity - soldQuantity);
        if (inventoryBatchVO.getExpirationTime() != null) {
            long days = ChronoUnit.DAYS.between(LocalDate.now(), inventoryBatchVO.getExpirationTime().toLocalDate());
            inventoryBatchVO.setDaysToExpire((int) days);
        }
        if (inventoryBatchVO.getProductId() != null && inventoryBatchVO.getId() != null) {
            inventoryBatchVO.setDynamicPrice(pricingRuleService.calculateDynamicPrice(inventoryBatchVO.getProductId(), inventoryBatchVO.getId()));
        }
    }
}
