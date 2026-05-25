package com.shelfflow.mapper;

import com.shelfflow.entity.ProductSpec;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductSpecMapper {

    void insertBatch(List<ProductSpec> list);

    @Delete("delete from product_spec where product_id = #{id}")
    void deleteByProductId(Long id);

    @Select("select * from product_spec where product_id = #{id}")
    List<ProductSpec> getByProductId(Long id);
}
