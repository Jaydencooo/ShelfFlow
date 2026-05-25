package com.shelfflow.mapper;

import com.github.pagehelper.Page;
import com.shelfflow.anno.AutoFill;
import com.shelfflow.dto.BundleDTO;
import com.shelfflow.dto.BundlePageQueryDTO;
import com.shelfflow.entity.Bundle;
import com.shelfflow.enumeration.OperationType;
import com.shelfflow.vo.ProductItemVO;
import com.shelfflow.vo.BundleVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BundleMapper {


    Page<BundleVO> bundlePageQuery(BundlePageQueryDTO bundlePageQueryDTO);

    @AutoFill(OperationType.INSERT)
    void add(Bundle bundle);

    @Select("select status from bundle where id = #{id}")
    Integer getStatusById(Long id);

    @Delete("delete from bundle where id = #{id}")
    void deleteById(Long id);

    @Select("select * from bundle where id = #{id}")
    BundleVO getById(Long id);

    @AutoFill(OperationType.UPDATE)
    void update(Bundle bundle);


    void setStatusByIds(Integer status, List<Long> ids);

    List<Bundle> getList(Bundle bundle);

    @Select("select sd.name, sd.copies, d.image, d.description " +
            "from bundle_product sd " +
            "left outer join product d " +
            "on sd.product_id = d.id " +
            "where sd.bundle_id = #{bundleId}")
    List<ProductItemVO> getProductsByBundleId(Long bundleId);

    @Select("select count(id) from bundle where status = #{status}")
    Integer countByStatus(Integer status);
}
