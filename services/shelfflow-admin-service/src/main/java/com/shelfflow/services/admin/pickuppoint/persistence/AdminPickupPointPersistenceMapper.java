package com.shelfflow.services.admin.pickuppoint.persistence;

import com.shelfflow.services.admin.pickuppoint.persistence.dataobject.AdminPickupPointDataObject;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AdminPickupPointPersistenceMapper {

    List<AdminPickupPointDataObject> listAll();

    AdminPickupPointDataObject findById(@Param("id") Long id);

    Long findIdByName(@Param("name") String name);

    Integer findMaxSort();

    int insert(AdminPickupPointDataObject pickupPoint);

    int update(AdminPickupPointDataObject pickupPoint);

    int disable(@Param("id") Long id, @Param("actorId") Long actorId);
}
