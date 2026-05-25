package com.shelfflow.mapper;

import com.shelfflow.entity.CartItem;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CartItemMapper {
    List<CartItem> list(CartItem cartItem);

    @Update("update cart_item set number = #{number} where id = #{id}")
    void setNumberById(CartItem cartItem);

    @Insert("insert into cart_item(name, image, user_id, product_id, bundle_id, batch_id, product_spec, number, amount, create_time) " +
            "VALUES(#{name}, #{image}, #{userId},#{productId},#{bundleId},#{batchId},#{productSpec},#{number},#{amount},#{createTime}) ")
    void insert(CartItem cartItem);

    @Delete("delete from cart_item where user_id=#{userId}")
    void cleanByUserId(Long userId);

    void deleteItem(CartItem cartItem);

    void insertBatch(List<CartItem> list);
}
