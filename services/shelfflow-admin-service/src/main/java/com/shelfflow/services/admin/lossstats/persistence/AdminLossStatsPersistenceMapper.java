package com.shelfflow.services.admin.lossstats.persistence;

import com.shelfflow.services.admin.lossstats.persistence.dataobject.AdminLossStatsCategoryRow;
import com.shelfflow.services.admin.lossstats.persistence.dataobject.AdminLossStatsOverviewRow;
import com.shelfflow.services.admin.lossstats.persistence.dataobject.AdminLossStatsSuggestionRow;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AdminLossStatsPersistenceMapper {

    AdminLossStatsOverviewRow overview(@Param("expiringSoonDays") int expiringSoonDays);

    List<AdminLossStatsCategoryRow> categoryStats(@Param("expiringSoonDays") int expiringSoonDays);

    List<AdminLossStatsSuggestionRow> suggestions(@Param("expiringSoonDays") int expiringSoonDays,
                                                  @Param("limit") int limit);
}
