package com.shelfflow.service.impl;

import com.fasterxml.jackson.databind.util.BeanUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.shelfflow.constant.MessageConstant;
import com.shelfflow.constant.StatusConstant;
import com.shelfflow.dto.ProductDTO;
import com.shelfflow.dto.ProductPageQueryDTO;
import com.shelfflow.entity.Bundle;
import com.shelfflow.entity.Product;
import com.shelfflow.entity.ProductSpec;
import com.shelfflow.exception.DeletionNotAllowedException;
import com.shelfflow.mapper.BundleMapper;
import com.shelfflow.mapper.BundleProductMapper;
import com.shelfflow.mapper.InventoryBatchMapper;
import com.shelfflow.mapper.ProductSpecMapper;
import com.shelfflow.mapper.ProductMapper;
import com.shelfflow.result.PageResult;
import com.shelfflow.service.PricingRuleService;
import com.shelfflow.service.ProductService;
import com.shelfflow.vo.InventoryBatchVO;
import com.shelfflow.vo.ProductVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ProductSpecMapper productSpecMapper;
    @Autowired
    private BundleProductMapper bundleProductMapper;
    @Autowired
    private BundleMapper bundleMapper;
    @Autowired
    private InventoryBatchMapper inventoryBatchMapper;
    @Autowired
    private PricingRuleService pricingRuleService;

    @Override
    @Transactional
    public void add(ProductDTO productDTO){
        Product product = new Product();
        BeanUtils.copyProperties(productDTO, product);

        productMapper.insert(product);
        Long id = product.getId();

        List<ProductSpec> list = productDTO.getFlavors();
        if (list != null && !list.isEmpty()) {
            list.forEach(productSpec -> {
                productSpec.setProductId(id);
            });
            productSpecMapper.insertBatch(list);
        }

    }

    @Override
    public PageResult productPageQuery(ProductPageQueryDTO productPageQueryDTO){
        PageHelper.startPage(productPageQueryDTO.getPage(),productPageQueryDTO.getPageSize());
        Page<ProductVO> page = productMapper.productPageQuery(productPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        //product的状态为起售中，不能删除
        for(Long id: ids){
            Product product = productMapper.getById(id);
            if(product == null){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ID_NOT_EXISTS);
            }else if(product.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        //product的id有相关联的bundle，不能删除
        List<Long> list = bundleProductMapper.getIdsByProductIds(ids);
        if(list != null && !list.isEmpty()){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        for(Long id: ids){
            productMapper.deleteById(id);
            productSpecMapper.deleteByProductId(id);
        }
    }

    @Override
    public ProductVO getById(Long id){
        Product product = productMapper.getById(id);
        List<ProductSpec> flavors = productSpecMapper.getByProductId(id);
        ProductVO productVO = new ProductVO();
        BeanUtils.copyProperties(product,productVO);
        productVO.setFlavors(flavors);
        return productVO;
    }

    @Override
    @Transactional
    public void update(ProductDTO productDTO) {
        Product product = new Product();
        BeanUtils.copyProperties(productDTO, product);
        productMapper.update(product);//update不会改变id，不像insert那样插入后返回生成的id

        Long id = productDTO.getId();
        productSpecMapper.deleteByProductId(id);
        List<ProductSpec> flavors = productDTO.getFlavors();

        if(flavors != null && !flavors.isEmpty()){
            flavors.forEach(flavor -> {
                flavor.setProductId(id);
            });
            productSpecMapper.insertBatch(flavors);
        }
    }

    @Override
    public void setStatusById(Integer status, Long id) {
        Product product = Product.builder().id(id).status(status).build();
        productMapper.update(product);
        if(status == StatusConstant.DISABLE){
            List<Long> productIds = new ArrayList<>();
            productIds.add(id);
            List<Long> bundleIds = bundleProductMapper.getIdsByProductIds(productIds);
            bundleMapper.setStatusByIds(status, bundleIds);
        }
    }

    @Override
    public List<Product> getByCategoryId(Long categoryId) {
        Product product = Product.builder().categoryId(categoryId).status(StatusConstant.ENABLE).build();
        List<Product> list = productMapper.getList(product);
        return list;
    }

    @Override
    public List<ProductVO> getListByCategoryId(Long categoryId) {
        Product queryProduct = Product.builder().categoryId(categoryId).status(StatusConstant.ENABLE).build();
        List<Product> products = productMapper.getList(queryProduct);
        List<ProductVO> productVOs = new ArrayList<>();
        for(Product product : products){
            List<ProductSpec> productSpecs = productSpecMapper.getByProductId(product.getId());
            ProductVO productVO = ProductVO.builder().flavors(productSpecs).build();
            BeanUtils.copyProperties(product,productVO);
            fillShelfFlowSaleFields(productVO);
            if (productVO.getAvailableQuantity() != null && productVO.getAvailableQuantity() > 0) {
                productVOs.add(productVO);
            }
        }
        return productVOs;
    }

    private void fillShelfFlowSaleFields(ProductVO productVO) {
        List<InventoryBatchVO> batches = inventoryBatchMapper.listByProductId(productVO.getId());
        if (batches == null || batches.isEmpty()) {
            productVO.setAvailableQuantity(0);
            return;
        }
        InventoryBatchVO bestBatch = null;
        int totalAvailableQuantity = 0;
        LocalDate today = LocalDate.now();
        for (InventoryBatchVO batch : batches) {
            int stockQuantity = batch.getStockQuantity() == null ? 0 : batch.getStockQuantity();
            int lockedQuantity = batch.getLockedQuantity() == null ? 0 : batch.getLockedQuantity();
            int soldQuantity = batch.getSoldQuantity() == null ? 0 : batch.getSoldQuantity();
            int availableQuantity = stockQuantity - lockedQuantity - soldQuantity;
            if (availableQuantity <= 0 || batch.getStatus() == null || batch.getStatus() != StatusConstant.ENABLE || batch.getExpirationTime() == null) {
                continue;
            }
            if (batch.getExpirationTime().toLocalDate().isBefore(today)) {
                continue;
            }
            totalAvailableQuantity += availableQuantity;
            if (bestBatch == null || batch.getExpirationTime().isBefore(bestBatch.getExpirationTime())) {
                bestBatch = batch;
            }
        }
        productVO.setAvailableQuantity(totalAvailableQuantity);
        if (bestBatch != null) {
            productVO.setBatchId(bestBatch.getId());
            productVO.setNearestExpirationTime(bestBatch.getExpirationTime());
            productVO.setDaysToExpire((int) ChronoUnit.DAYS.between(today, bestBatch.getExpirationTime().toLocalDate()));
            productVO.setDynamicPrice(pricingRuleService.calculateDynamicPrice(productVO.getId(), bestBatch.getId()));
        }
    }

}
