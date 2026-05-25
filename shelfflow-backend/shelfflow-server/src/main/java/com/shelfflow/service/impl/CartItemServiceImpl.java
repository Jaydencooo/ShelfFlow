package com.shelfflow.service.impl;

import com.shelfflow.context.CurrentActorContext;
import com.shelfflow.context.CurrentActorContext;
import com.shelfflow.dto.CartItemDTO;
import com.shelfflow.entity.Product;
import com.shelfflow.entity.Bundle;
import com.shelfflow.entity.CartItem;
import com.shelfflow.mapper.ProductMapper;
import com.shelfflow.mapper.BundleMapper;
import com.shelfflow.mapper.CartItemMapper;
import com.shelfflow.service.CartItemService;
import com.shelfflow.vo.BundleVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CartItemServiceImpl implements CartItemService {
    @Autowired
    private CartItemMapper cartItemMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private BundleMapper bundleMapper;

    @Override
    public void add(CartItemDTO cartItemDTO){
        CartItem cartItem = new CartItem();
        BeanUtils.copyProperties(cartItemDTO,cartItem);
        cartItem.setUserId(CurrentActorContext.getCurrentId());
        List<CartItem> list = cartItemMapper.list(cartItem);
        //返回list结果，防止意外情况有重复数据。实际之取第一条
        if(list != null && list.size() == 1){//选品车中已经有一条了（但是number可能大于1）
            cartItem = list.get(0);
            cartItem.setNumber(cartItem.getNumber() + 1);
            cartItemMapper.setNumberById(cartItem);
        }else{//选品车中没有此商品/组合包
            Long productId = cartItem.getProductId();
            if(productId != null){//是商品
                Product product = productMapper.getById(productId);
                cartItem.setName(product.getName());
                cartItem.setImage(product.getImage());
                cartItem.setAmount(product.getPrice());
            }else{//是组合包
                BundleVO bundleVO = bundleMapper.getById(cartItem.getBundleId());
                cartItem.setName(bundleVO.getName());
                cartItem.setImage(bundleVO.getImage());
                cartItem.setAmount(bundleVO.getPrice());
            }
            cartItem.setNumber(1);
            cartItem.setCreateTime(LocalDateTime.now());
            cartItemMapper.insert(cartItem);

        }
    }

    @Override
    public List<CartItem> list() {
        CartItem cartItem = CartItem.builder().userId(CurrentActorContext.getCurrentId()).build();
        return cartItemMapper.list(cartItem);
    }

    @Override
    public void clean() {
        cartItemMapper.cleanByUserId(CurrentActorContext.getCurrentId());
    }

    @Override
    public void sub(CartItemDTO cartItemDTO) {
        CartItem cartItem = new CartItem();
        BeanUtils.copyProperties(cartItemDTO, cartItem);
        cartItem.setUserId(CurrentActorContext.getCurrentId());
        List<CartItem> list = cartItemMapper.list(cartItem);

        if(list != null && list.size() == 1){
            cartItem = list.get(0);
            Integer number = cartItem.getNumber();
            if( number == 1){//直接删除
                cartItemMapper.deleteItem(cartItem);
            }else if(number > 1){//number--
                cartItem.setNumber(cartItem.getNumber() - 1 );
                cartItemMapper.setNumberById(cartItem);
            }

        }
    }
}
