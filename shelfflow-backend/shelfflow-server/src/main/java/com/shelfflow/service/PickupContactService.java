package com.shelfflow.service;

import com.shelfflow.entity.PickupContact;
import com.shelfflow.result.Result;

import java.util.List;

public interface PickupContactService {

    List<PickupContact> list();

    List<PickupContact> defaultList();

    void add(PickupContact pickupContact);

    void setDefaultById(PickupContact pickupContact);

    void update(PickupContact pickupContact);

    PickupContact getById(Long id);

    void deleteById(Long id);
}
