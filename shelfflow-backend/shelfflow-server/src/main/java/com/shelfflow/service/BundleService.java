package com.shelfflow.service;


import com.shelfflow.dto.BundleDTO;
import com.shelfflow.dto.BundlePageQueryDTO;
import com.shelfflow.entity.Bundle;
import com.shelfflow.result.PageResult;
import com.shelfflow.vo.ProductItemVO;
import com.shelfflow.vo.ProductOverviewVO;
import com.shelfflow.vo.BundleVO;

import java.util.List;

public interface BundleService {
    public PageResult bundlePageQuery(BundlePageQueryDTO bundlePageQueryDTO);

    void add(BundleDTO bundleDTO);

    void deleteBatch(List<Long> ids);

    BundleVO getById(Long id);

    void update(BundleDTO bundleDTO);

    void setStatusById(Integer status, Long id);

    //用户端用，主界面上
    List<Bundle> getListByCategoryId(Long categoryId);

    List<ProductItemVO> getProductsByBundleId(Long id);
}
