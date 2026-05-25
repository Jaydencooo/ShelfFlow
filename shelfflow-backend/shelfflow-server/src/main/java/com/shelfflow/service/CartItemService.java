package com.shelfflow.service;

import com.shelfflow.dto.CartItemDTO;
import com.shelfflow.entity.CartItem;

import java.util.List;

public interface CartItemService {
    void add(CartItemDTO cartItemDTO);

    List<CartItem> list();

    void clean();

    void sub(CartItemDTO cartItemDTO);
}
