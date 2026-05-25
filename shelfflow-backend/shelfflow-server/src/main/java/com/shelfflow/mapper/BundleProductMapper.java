package com.shelfflow.mapper;

import com.shelfflow.entity.BundleProduct;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BundleProductMapper {

    List<Long> getIdsByProductIds(List<Long> ids);

    @Select("select product_id from bundle_product where bundle_id = #{bundleId}")
    List<Long> getProductIdsByBundleId(Long bundleId);

    void add(List<BundleProduct> bundleProducts);

    @Delete("delete from bundle_product where bundle_id = #{id}")
    void deleteByBundleId(Long id);

    @Select("select * from bundle_product where bundle_id = #{id}")
    List<BundleProduct> getByBundleId(Long id);
}
