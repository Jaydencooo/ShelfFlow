package com.shelfflow.mapper;

import com.github.pagehelper.Page;
import com.shelfflow.dto.FulfillmentTaskPageQueryDTO;
import com.shelfflow.entity.FulfillmentTask;
import com.shelfflow.vo.FulfillmentTaskVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface FulfillmentTaskMapper {

    void upsert(FulfillmentTask fulfillmentTask);

    void updateByOrderId(FulfillmentTask fulfillmentTask);

    Page<FulfillmentTaskVO> pageQuery(FulfillmentTaskPageQueryDTO fulfillmentTaskPageQueryDTO);

    @Select("select t.*, o.status as order_status, o.amount, o.phone, o.consignee, o.remark from fulfillment_task t left join orders o on t.order_id = o.id where t.order_id = #{orderId}")
    FulfillmentTaskVO getByOrderId(Long orderId);

    @Select("select status, count(1) as total from fulfillment_task group by status")
    List<Map<String, Object>> statusStatistics();
}
