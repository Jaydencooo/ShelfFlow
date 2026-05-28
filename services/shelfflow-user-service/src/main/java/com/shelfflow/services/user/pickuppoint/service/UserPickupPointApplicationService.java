package com.shelfflow.services.user.pickuppoint.service;

import com.shelfflow.services.common.dto.PickupPointResponse;
import com.shelfflow.services.user.pickuppoint.persistence.UserPickupPointPersistenceMapper;
import com.shelfflow.services.user.pickuppoint.persistence.dataobject.UserPickupPointDataObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserPickupPointApplicationService {

    private final UserPickupPointPersistenceMapper pickupPointPersistenceMapper;

    public UserPickupPointApplicationService(UserPickupPointPersistenceMapper pickupPointPersistenceMapper) {
        this.pickupPointPersistenceMapper = pickupPointPersistenceMapper;
    }

    @Transactional(readOnly = true)
    public List<PickupPointResponse> listEnabled() {
        return pickupPointPersistenceMapper.listEnabled().stream()
                .map(this::toResponse)
                .toList();
    }

    private PickupPointResponse toResponse(UserPickupPointDataObject row) {
        return PickupPointResponse.builder()
                .id(String.valueOf(row.getId()))
                .name(row.getName())
                .address(row.getAddress())
                .contactName(row.getContactName())
                .contactPhone(row.getContactPhone())
                .serviceTime(row.getServiceTime())
                .sort(row.getSort())
                .enabled(row.getStatus() != null && row.getStatus() == 1)
                .build();
    }
}
