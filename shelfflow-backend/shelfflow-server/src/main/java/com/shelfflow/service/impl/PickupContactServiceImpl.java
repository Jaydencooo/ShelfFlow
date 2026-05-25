package com.shelfflow.service.impl;

import com.shelfflow.context.CurrentActorContext;
import com.shelfflow.entity.PickupContact;
import com.shelfflow.mapper.PickupContactMapper;
import com.shelfflow.service.PickupContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PickupContactServiceImpl implements PickupContactService {
    @Autowired
    private PickupContactMapper pickupContactMapper;



    @Override
    public void add(PickupContact pickupContact) {
        pickupContact.setIsDefault(0);
        pickupContact.setUserId(CurrentActorContext.getCurrentId());
        pickupContactMapper.insert(pickupContact);
    }

    @Override
    public void setDefaultById(PickupContact pickupContact) {
        //设置其他所有为非默认
        pickupContact.setUserId(CurrentActorContext.getCurrentId());
        Long id = pickupContact.getId();
        pickupContact.setId(null);
        pickupContact.setIsDefault(0);
        pickupContactMapper.update(pickupContact);
        //设置当前联系人为默认
        pickupContact.setUserId(null);
        pickupContact.setId(id);
        pickupContact.setIsDefault(1);
        pickupContactMapper.update(pickupContact);
    }

    @Override
    public void update(PickupContact pickupContact) {
        pickupContactMapper.update(pickupContact);
    }

    @Override
    public PickupContact getById(Long id) {
        return pickupContactMapper.getById(id);
    }

    @Override
    public void deleteById(Long id) {
        pickupContactMapper.deleteById(id);
    }

    @Override
    public List<PickupContact> list(){
        PickupContact pickupContact = PickupContact.builder().userId(CurrentActorContext.getCurrentId()).build();
        return pickupContactMapper.list(pickupContact);
    }

    @Override
    public List<PickupContact> defaultList(){
        PickupContact pickupContact = PickupContact.builder()
                .userId(CurrentActorContext.getCurrentId())
                .isDefault(1)
                .build();
        return pickupContactMapper.list(pickupContact);
    }
}
