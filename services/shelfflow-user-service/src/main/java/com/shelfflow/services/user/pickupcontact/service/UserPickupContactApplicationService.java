package com.shelfflow.services.user.pickupcontact.service;

import com.shelfflow.services.common.dto.UserPickupContactRequest;
import com.shelfflow.services.common.dto.UserPickupContactResponse;
import com.shelfflow.services.common.security.UserAuthenticatedUser;
import com.shelfflow.services.user.pickupcontact.domain.UserPickupContactPolicy;
import com.shelfflow.services.user.pickupcontact.persistence.UserPickupContactPersistenceMapper;
import com.shelfflow.services.user.pickupcontact.persistence.dataobject.UserPickupContactDataObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserPickupContactApplicationService {

    private static final int DEFAULT_CONTACT_FLAG = 1;
    private static final int NON_DEFAULT_CONTACT_FLAG = 0;

    private final UserPickupContactPersistenceMapper userPickupContactPersistenceMapper;
    private final UserPickupContactPolicy userPickupContactPolicy;

    public UserPickupContactApplicationService(UserPickupContactPersistenceMapper userPickupContactPersistenceMapper,
                                               UserPickupContactPolicy userPickupContactPolicy) {
        this.userPickupContactPersistenceMapper = userPickupContactPersistenceMapper;
        this.userPickupContactPolicy = userPickupContactPolicy;
    }

    public List<UserPickupContactResponse> list(UserAuthenticatedUser authenticatedUser) {
        return userPickupContactPersistenceMapper.listByUserId(authenticatedUser.getUserId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public UserPickupContactResponse create(UserAuthenticatedUser authenticatedUser, UserPickupContactRequest request) {
        int currentCount = userPickupContactPersistenceMapper.countByUserId(authenticatedUser.getUserId());
        userPickupContactPolicy.ensureContactLimitNotExceeded(currentCount);

        boolean defaultContact = Boolean.TRUE.equals(request.getDefaultContact()) || currentCount == 0;
        if (defaultContact) {
            userPickupContactPersistenceMapper.clearDefaultByUserId(authenticatedUser.getUserId());
        }

        UserPickupContactDataObject contact = buildContact(authenticatedUser.getUserId(), request);
        contact.setIsDefault(defaultContact ? DEFAULT_CONTACT_FLAG : NON_DEFAULT_CONTACT_FLAG);
        userPickupContactPersistenceMapper.insert(contact);
        return toResponse(contact);
    }

    @Transactional
    public UserPickupContactResponse update(UserAuthenticatedUser authenticatedUser, String id, UserPickupContactRequest request) {
        Long contactId = userPickupContactPolicy.parseRequiredContactId(id);
        UserPickupContactDataObject existing = userPickupContactPersistenceMapper.findByIdAndUserId(contactId, authenticatedUser.getUserId());
        userPickupContactPolicy.ensureContactExists(existing != null);

        if (Boolean.TRUE.equals(request.getDefaultContact())) {
            userPickupContactPersistenceMapper.clearDefaultByUserId(authenticatedUser.getUserId());
        }

        UserPickupContactDataObject contact = buildContact(authenticatedUser.getUserId(), request);
        contact.setId(contactId);
        contact.setIsDefault(Boolean.TRUE.equals(request.getDefaultContact()) ? DEFAULT_CONTACT_FLAG : null);

        int affectedRows = userPickupContactPersistenceMapper.updateByIdAndUserId(contact);
        userPickupContactPolicy.ensureContactExists(affectedRows > 0);
        return toResponse(userPickupContactPersistenceMapper.findByIdAndUserId(contactId, authenticatedUser.getUserId()));
    }

    @Transactional
    public UserPickupContactResponse setDefault(UserAuthenticatedUser authenticatedUser, String id) {
        Long contactId = userPickupContactPolicy.parseRequiredContactId(id);
        UserPickupContactDataObject existing = userPickupContactPersistenceMapper.findByIdAndUserId(contactId, authenticatedUser.getUserId());
        userPickupContactPolicy.ensureContactExists(existing != null);

        userPickupContactPersistenceMapper.clearDefaultByUserId(authenticatedUser.getUserId());
        int affectedRows = userPickupContactPersistenceMapper.setDefaultByIdAndUserId(contactId, authenticatedUser.getUserId());
        userPickupContactPolicy.ensureContactExists(affectedRows > 0);
        return toResponse(userPickupContactPersistenceMapper.findByIdAndUserId(contactId, authenticatedUser.getUserId()));
    }

    @Transactional
    public void delete(UserAuthenticatedUser authenticatedUser, String id) {
        Long contactId = userPickupContactPolicy.parseRequiredContactId(id);
        UserPickupContactDataObject existing = userPickupContactPersistenceMapper.findByIdAndUserId(contactId, authenticatedUser.getUserId());
        userPickupContactPolicy.ensureContactExists(existing != null);

        int affectedRows = userPickupContactPersistenceMapper.deleteByIdAndUserId(contactId, authenticatedUser.getUserId());
        userPickupContactPolicy.ensureContactExists(affectedRows > 0);

        if (isDefault(existing)) {
            List<UserPickupContactDataObject> remaining = userPickupContactPersistenceMapper.listByUserId(authenticatedUser.getUserId());
            if (!remaining.isEmpty()) {
                userPickupContactPersistenceMapper.setDefaultByIdAndUserId(remaining.get(0).getId(), authenticatedUser.getUserId());
            }
        }
    }

    private UserPickupContactDataObject buildContact(Long userId, UserPickupContactRequest request) {
        UserPickupContactDataObject contact = new UserPickupContactDataObject();
        contact.setUserId(userId);
        contact.setConsignee(userPickupContactPolicy.normalizeConsignee(request.getConsignee()));
        contact.setPhone(userPickupContactPolicy.normalizePhone(request.getPhone()));
        contact.setLabel(userPickupContactPolicy.normalizeLabel(request.getLabel()));
        contact.setDetail(userPickupContactPolicy.normalizeDetail(request.getDetail()));
        return contact;
    }

    private UserPickupContactResponse toResponse(UserPickupContactDataObject contact) {
        return UserPickupContactResponse.builder()
                .id(String.valueOf(contact.getId()))
                .consignee(contact.getConsignee())
                .phone(contact.getPhone())
                .label(contact.getLabel())
                .detail(contact.getDetail())
                .defaultContact(isDefault(contact))
                .build();
    }

    private boolean isDefault(UserPickupContactDataObject contact) {
        return contact != null && Integer.valueOf(DEFAULT_CONTACT_FLAG).equals(contact.getIsDefault());
    }
}
