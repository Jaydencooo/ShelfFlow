package com.shelfflow.services.admin.pickuppoint.service;

import com.shelfflow.services.admin.pickuppoint.persistence.AdminPickupPointPersistenceMapper;
import com.shelfflow.services.admin.pickuppoint.persistence.dataobject.AdminPickupPointDataObject;
import com.shelfflow.services.common.api.ErrorCode;
import com.shelfflow.services.common.dto.PickupPointResponse;
import com.shelfflow.services.common.dto.PickupPointUpsertRequest;
import com.shelfflow.services.common.exception.ApplicationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminPickupPointApplicationService {

    private static final int ENABLED_STATUS = 1;
    private static final int DISABLED_STATUS = 0;
    private static final int SORT_STEP = 10;

    private final AdminPickupPointPersistenceMapper pickupPointPersistenceMapper;

    public AdminPickupPointApplicationService(AdminPickupPointPersistenceMapper pickupPointPersistenceMapper) {
        this.pickupPointPersistenceMapper = pickupPointPersistenceMapper;
    }

    @Transactional(readOnly = true)
    public List<PickupPointResponse> listAll() {
        return pickupPointPersistenceMapper.listAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public PickupPointResponse create(Long actorId, PickupPointUpsertRequest request) {
        String normalizedName = normalizeRequired(request.getName(), "自提点名称");
        ensureUniqueName(normalizedName, null);

        LocalDateTime now = LocalDateTime.now();
        AdminPickupPointDataObject pickupPoint = new AdminPickupPointDataObject();
        pickupPoint.setName(normalizedName);
        pickupPoint.setAddress(normalizeRequired(request.getAddress(), "自提点地址"));
        pickupPoint.setContactName(normalizeOptional(request.getContactName()));
        pickupPoint.setContactPhone(normalizeOptional(request.getContactPhone()));
        pickupPoint.setServiceTime(normalizeOptional(request.getServiceTime()));
        pickupPoint.setSort(request.getSort() == null ? nextSort() : request.getSort());
        pickupPoint.setStatus(Boolean.FALSE.equals(request.getEnabled()) ? DISABLED_STATUS : ENABLED_STATUS);
        pickupPoint.setCreateTime(now);
        pickupPoint.setUpdateTime(now);
        pickupPoint.setCreateUser(actorId);
        pickupPoint.setUpdateUser(actorId);
        pickupPointPersistenceMapper.insert(pickupPoint);
        return toResponse(pickupPointPersistenceMapper.findById(pickupPoint.getId()));
    }

    @Transactional
    public PickupPointResponse update(Long actorId, String id, PickupPointUpsertRequest request) {
        Long pickupPointId = parseRequiredLong(id, "id");
        AdminPickupPointDataObject existing = pickupPointPersistenceMapper.findById(pickupPointId);
        if (existing == null) {
            throw new ApplicationException(ErrorCode.NOT_FOUND, "自提点不存在");
        }

        String normalizedName = normalizeRequired(request.getName(), "自提点名称");
        ensureUniqueName(normalizedName, pickupPointId);

        AdminPickupPointDataObject pickupPoint = new AdminPickupPointDataObject();
        pickupPoint.setId(pickupPointId);
        pickupPoint.setName(normalizedName);
        pickupPoint.setAddress(normalizeRequired(request.getAddress(), "自提点地址"));
        pickupPoint.setContactName(normalizeOptional(request.getContactName()));
        pickupPoint.setContactPhone(normalizeOptional(request.getContactPhone()));
        pickupPoint.setServiceTime(normalizeOptional(request.getServiceTime()));
        pickupPoint.setSort(request.getSort() == null ? existing.getSort() : request.getSort());
        pickupPoint.setStatus(Boolean.FALSE.equals(request.getEnabled()) ? DISABLED_STATUS : ENABLED_STATUS);
        pickupPoint.setUpdateTime(LocalDateTime.now());
        pickupPoint.setUpdateUser(actorId);
        pickupPointPersistenceMapper.update(pickupPoint);
        return toResponse(pickupPointPersistenceMapper.findById(pickupPointId));
    }

    @Transactional
    public void disable(Long actorId, String id) {
        Long pickupPointId = parseRequiredLong(id, "id");
        if (pickupPointPersistenceMapper.disable(pickupPointId, actorId) == 0) {
            throw new ApplicationException(ErrorCode.NOT_FOUND, "自提点不存在");
        }
    }

    private void ensureUniqueName(String name, Long currentId) {
        Long existingId = pickupPointPersistenceMapper.findIdByName(name);
        if (existingId != null && !existingId.equals(currentId)) {
            throw new ApplicationException(ErrorCode.CONFLICT, "自提点名称已存在");
        }
    }

    private int nextSort() {
        Integer maxSort = pickupPointPersistenceMapper.findMaxSort();
        return (maxSort == null ? 0 : maxSort) + SORT_STEP;
    }

    private PickupPointResponse toResponse(AdminPickupPointDataObject row) {
        return PickupPointResponse.builder()
                .id(String.valueOf(row.getId()))
                .name(row.getName())
                .address(row.getAddress())
                .contactName(row.getContactName())
                .contactPhone(row.getContactPhone())
                .serviceTime(row.getServiceTime())
                .sort(row.getSort())
                .enabled(row.getStatus() != null && row.getStatus() == ENABLED_STATUS)
                .createTime(row.getCreateTime())
                .updateTime(row.getUpdateTime())
                .build();
    }

    private String normalizeRequired(String value, String fieldName) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, fieldName + "不能为空");
        }
        return normalized;
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Long parseRequiredLong(String value, String fieldName) {
        String normalized = normalizeRequired(value, fieldName);
        try {
            return Long.valueOf(normalized);
        } catch (NumberFormatException exception) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, fieldName + " 不是有效数字");
        }
    }
}
