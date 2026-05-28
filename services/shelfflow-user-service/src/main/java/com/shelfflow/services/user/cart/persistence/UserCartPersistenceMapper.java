package com.shelfflow.services.user.cart.persistence;

import com.shelfflow.services.user.cart.persistence.dataobject.UserCartItemDataObject;
import com.shelfflow.services.user.cart.persistence.dataobject.UserCartItemRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserCartPersistenceMapper {

    List<UserCartItemRow> listByUserId(@Param("userId") Long userId);

    UserCartItemDataObject findBySelection(@Param("userId") Long userId,
                                           @Param("productId") Long productId,
                                           @Param("batchId") Long batchId,
                                           @Param("productSpec") String productSpec);

    UserCartItemRow findRowByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    void insert(UserCartItemDataObject cartItem);

    int updateQuantityById(UserCartItemDataObject cartItem);

    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    int deleteByIdsAndUserId(@Param("ids") List<Long> ids, @Param("userId") Long userId);

    int clearByUserId(@Param("userId") Long userId);
}
