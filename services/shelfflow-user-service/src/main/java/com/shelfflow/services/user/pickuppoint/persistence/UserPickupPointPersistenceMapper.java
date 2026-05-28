package com.shelfflow.services.user.pickuppoint.persistence;

import com.shelfflow.services.user.pickuppoint.persistence.dataobject.UserPickupPointDataObject;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserPickupPointPersistenceMapper {

    List<UserPickupPointDataObject> listEnabled();

    UserPickupPointDataObject findEnabledById(@Param("id") Long id);
}
