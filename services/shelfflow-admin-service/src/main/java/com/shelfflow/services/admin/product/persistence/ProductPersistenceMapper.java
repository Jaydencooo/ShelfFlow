package com.shelfflow.services.admin.product.persistence;

import com.shelfflow.services.admin.product.persistence.dataobject.ProductDataObject;
import com.shelfflow.services.admin.product.persistence.dataobject.ProductCategoryDataObject;
import com.shelfflow.services.admin.product.persistence.dataobject.ProductCategoryRow;
import com.shelfflow.services.admin.product.persistence.dataobject.ProductPageCriteria;
import com.shelfflow.services.admin.product.persistence.dataobject.ProductPageRow;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductPersistenceMapper {

    List<ProductPageRow> page(@Param("criteria") ProductPageCriteria criteria);

    long count(@Param("criteria") ProductPageCriteria criteria);

    List<ProductCategoryRow> listActiveProductCategories();

    ProductCategoryDataObject findCategoryById(@Param("id") Long id);

    Long findCategoryIdByName(@Param("name") String name);

    Integer findMaxCategorySort();

    ProductDataObject findById(@Param("id") Long id);

    Long findIdByName(@Param("name") String name);

    boolean existsCategory(@Param("categoryId") Long categoryId);

    long countProductsByCategory(@Param("categoryId") Long categoryId);

    long countBatchesByProduct(@Param("productId") Long productId);

    int insert(ProductDataObject product);

    int insertCategory(ProductCategoryDataObject category);

    int update(ProductDataObject product);

    int updateProductStatus(@Param("id") Long id,
                            @Param("status") Integer status,
                            @Param("updateUser") Long updateUser);

    int pauseActiveBatchesByProduct(@Param("productId") Long productId,
                                    @Param("updateUser") Long updateUser);

    int logicallyDeleteProduct(@Param("id") Long id,
                               @Param("deletedStatus") Integer deletedStatus,
                               @Param("updateUser") Long updateUser);

    int disableCategory(@Param("id") Long id,
                        @Param("updateUser") Long updateUser);
}
