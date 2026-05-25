package com.shelfflow.mapper;


import com.github.pagehelper.Page;
import com.shelfflow.anno.AutoFill;
import com.shelfflow.dto.ProductDTO;
import com.shelfflow.dto.ProductPageQueryDTO;
import com.shelfflow.entity.Product;
import com.shelfflow.enumeration.OperationType;
import com.shelfflow.vo.ProductVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductMapper {

    @AutoFill(OperationType.INSERT)
    void insert(Product product);

    Page<ProductVO> productPageQuery(ProductPageQueryDTO productPageQueryDTO);

    @Select("select * from product where id=#{id}")
    Product getById(Long id);

    @Select("select status from product where id=#{id}")
    Integer getStatusById(Long id);

    @Delete("delete from product where id=#{id}")
    void deleteById(Long id);

    @AutoFill(OperationType.UPDATE)
    void update(Product product);

    List<Product> getList(Product product);

    @Select("select count(id) from product where status = #{status}")
    Integer countByStatus(Integer status);
}
