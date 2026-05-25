package com.shelfflow.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.shelfflow.constant.MessageConstant;
import com.shelfflow.constant.StatusConstant;
import com.shelfflow.dto.BundleDTO;
import com.shelfflow.dto.BundlePageQueryDTO;
import com.shelfflow.entity.Bundle;
import com.shelfflow.entity.BundleProduct;
import com.shelfflow.exception.DeletionNotAllowedException;
import com.shelfflow.exception.BundleEnableFailedException;
import com.shelfflow.mapper.ProductMapper;
import com.shelfflow.mapper.BundleProductMapper;
import com.shelfflow.mapper.BundleMapper;
import com.shelfflow.result.PageResult;
import com.shelfflow.service.BundleService;
import com.shelfflow.vo.ProductItemVO;
import com.shelfflow.vo.ProductOverviewVO;
import com.shelfflow.vo.BundleVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class BundleServiceImpl implements BundleService {
    @Autowired
    private BundleMapper bundleMapper;

    @Autowired
    private BundleProductMapper bundleProductMapper;

    @Autowired
    private ProductMapper productMapper;

    public PageResult bundlePageQuery(BundlePageQueryDTO bundlePageQueryDTO){
        PageHelper.startPage(bundlePageQueryDTO.getPage(),bundlePageQueryDTO.getPageSize());
        Page<BundleVO> page = bundleMapper.bundlePageQuery(bundlePageQueryDTO);

        System.out.println("!!!!!!!!");
        System.out.println(new PageResult(page.getTotal(), page.getResult()));
        System.out.println("!!!!!!!!!!!1");

        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    @Transactional
    public void add(BundleDTO bundleDTO) {
        Bundle bundle = Bundle.builder().status(StatusConstant.DISABLE).build();
        BeanUtils.copyProperties(bundleDTO,bundle);
        bundleMapper.add(bundle);

        Long bundleId = bundle.getId();
        List<BundleProduct> bundleProducts = bundleDTO.getBundleProducts();
        for(BundleProduct s: bundleProducts){
            s.setBundleId(bundleId);
        }
        bundleProductMapper.add(bundleProducts);

    }

    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        ids.forEach(id -> {
            if(bundleMapper.getStatusById(id) == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        });
        ids.forEach(id ->{
            bundleMapper.deleteById(id);
            bundleProductMapper.deleteByBundleId(id);
        });
    }

    @Override
    public BundleVO getById(Long id) {
        BundleVO bundleVO = bundleMapper.getById(id);
        bundleVO.setBundleProducts(bundleProductMapper.getByBundleId(id));
        return bundleVO;
    }

    @Override
    @Transactional
    public void update(BundleDTO bundleDTO) {
        Bundle bundle = new Bundle();
        BeanUtils.copyProperties(bundleDTO,bundle);
        bundleMapper.update(bundle);

        Long bundleId = bundleDTO.getId();
        bundleProductMapper.deleteByBundleId(bundleId);

        List<BundleProduct> bundleProducts = bundleDTO.getBundleProducts();
        for(BundleProduct bundleProduct : bundleProducts){
            bundleProduct.setBundleId(bundleId);
        }
        bundleProductMapper.add(bundleProducts);
    }

    @Override
    public void setStatusById(Integer status, Long id) {
        if(status == StatusConstant.ENABLE){
           List<Long> productIds = bundleProductMapper.getProductIdsByBundleId(id);
           productIds.forEach(productId -> {
               if(productMapper.getStatusById(productId) == StatusConstant.DISABLE){
                   throw new BundleEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
               }
           });
        }

        Bundle bundle = Bundle.builder().status(status).id(id).build();
        bundleMapper.update(bundle);
    }

    @Override
    public List<Bundle> getListByCategoryId(Long categoryId){
        Bundle queryBundle = Bundle.builder().categoryId(categoryId).status(StatusConstant.ENABLE).build();
        return bundleMapper.getList(queryBundle);
    }

    @Override
    public List<ProductItemVO> getProductsByBundleId(Long bundleId) {
        return bundleMapper.getProductsByBundleId(bundleId);
    }


}
