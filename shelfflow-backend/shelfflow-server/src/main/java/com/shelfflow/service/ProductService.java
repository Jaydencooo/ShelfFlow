package com.shelfflow.service;

import com.shelfflow.dto.ProductDTO;
import com.shelfflow.dto.ProductPageQueryDTO;
import com.shelfflow.entity.Product;
import com.shelfflow.result.PageResult;
import com.shelfflow.vo.ProductVO;

import java.util.List;

public interface ProductService {
    void add(ProductDTO productDTO);

    PageResult productPageQuery(ProductPageQueryDTO productPageQueryDTO);

    void deleteBatch(List<Long> ids);

    ProductVO getById(Long id);

    void update(ProductDTO productDTO);

    void setStatusById(Integer status, Long id);

    //管理端，新增组合包的时候，选择关联商品的界面，需要根据CategoryId查询商品。返回Product即可
    List<Product> getByCategoryId(Long categoryId);

    //用户端，主界面根据分类categoryId查询商品。需要返回ProductVO（包含flavor，后续客户点单要选flavor）
    List<ProductVO> getListByCategoryId(Long categoryId);
}
