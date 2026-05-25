package com.shelfflow.services.admin.pricing.persistence;

import com.shelfflow.services.admin.pricing.persistence.dataobject.AdminPricingRuleCriteria;
import com.shelfflow.services.admin.pricing.persistence.dataobject.AdminPricingRuleDataObject;
import com.shelfflow.services.admin.pricing.persistence.dataobject.AdminPricingSuggestionRow;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AdminPricingRulePersistenceMapper {

    List<AdminPricingRuleDataObject> page(@Param("criteria") AdminPricingRuleCriteria criteria);

    long count(@Param("criteria") AdminPricingRuleCriteria criteria);

    AdminPricingRuleDataObject findById(@Param("id") Long id);

    Long findIdByName(@Param("name") String name);

    int insert(AdminPricingRuleDataObject rule);

    int update(AdminPricingRuleDataObject rule);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status, @Param("actorId") Long actorId);

    int deleteById(@Param("id") Long id);

    List<AdminPricingSuggestionRow> listSuggestions(@Param("limit") int limit,
                                                    @Param("acceptedRuleNamePrefix") String acceptedRuleNamePrefix);
}
