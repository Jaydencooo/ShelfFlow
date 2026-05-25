package com.shelfflow.services.user.catalog.persistence;

import com.shelfflow.services.user.catalog.persistence.dataobject.UserCatalogPricingRuleRow;
import com.shelfflow.services.user.cart.persistence.dataobject.UserCartProductSelectionRow;
import com.shelfflow.services.user.catalog.persistence.dataobject.UserCatalogProductCriteria;
import com.shelfflow.services.user.catalog.persistence.dataobject.UserCatalogProductDetailRow;
import com.shelfflow.services.user.catalog.persistence.dataobject.UserCatalogProductRow;
import com.shelfflow.services.user.catalog.persistence.dataobject.UserCatalogProductSpecRow;
import com.shelfflow.services.user.catalog.persistence.dataobject.UserCategoryRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserCatalogPersistenceMapper {

    List<UserCategoryRow> listActiveCategories(@Param("type") int type);

    List<UserCatalogProductRow> pageActiveProducts(@Param("criteria") UserCatalogProductCriteria criteria);

    long countActiveProducts(@Param("criteria") UserCatalogProductCriteria criteria);

    List<UserCatalogPricingRuleRow> listEnabledPricingRules();

    UserCatalogProductDetailRow findSellableProductDetailById(@Param("productId") Long productId);

    List<UserCatalogProductSpecRow> listProductSpecs(@Param("productId") Long productId);

    UserCartProductSelectionRow findRecommendedSellableBatchSelection(@Param("productId") Long productId);

    UserCartProductSelectionRow findSellableBatchSelection(@Param("productId") Long productId,
                                                           @Param("batchId") Long batchId);
}
