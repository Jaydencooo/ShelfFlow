package com.shelfflow.mapper;

import com.shelfflow.dto.ProductFlowDTO;
import com.shelfflow.entity.OrderDetail;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderDetailMapper {

    void insertBatch(List<OrderDetail> orderDetails);

    @Select("select * from order_detail where order_id=#{orderId}")
    List<OrderDetail> getByOrderId(Long orderId);

    List<ProductFlowDTO> getTop10ByMap(Map map);
}
