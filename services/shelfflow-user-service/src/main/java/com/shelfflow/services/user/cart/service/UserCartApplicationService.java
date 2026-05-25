package com.shelfflow.services.user.cart.service;

import com.shelfflow.services.common.dto.UserCartItemAddRequest;
import com.shelfflow.services.common.dto.UserCartItemQuantityUpdateRequest;
import com.shelfflow.services.common.dto.UserCartItemResponse;
import com.shelfflow.services.common.security.UserAuthenticatedUser;
import com.shelfflow.services.user.cart.domain.UserCartPolicy;
import com.shelfflow.services.user.cart.persistence.UserCartPersistenceMapper;
import com.shelfflow.services.user.cart.persistence.dataobject.UserCartItemDataObject;
import com.shelfflow.services.user.cart.persistence.dataobject.UserCartItemRow;
import com.shelfflow.services.user.cart.persistence.dataobject.UserCartProductSelectionRow;
import com.shelfflow.services.user.catalog.domain.UserCatalogPolicy;
import com.shelfflow.services.user.catalog.persistence.UserCatalogPersistenceMapper;
import com.shelfflow.services.user.catalog.persistence.dataobject.UserCatalogPricingRuleRow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserCartApplicationService {

    private final UserCartPersistenceMapper userCartPersistenceMapper;
    private final UserCatalogPersistenceMapper userCatalogPersistenceMapper;
    private final UserCartPolicy userCartPolicy;
    private final UserCatalogPolicy userCatalogPolicy;

    public UserCartApplicationService(UserCartPersistenceMapper userCartPersistenceMapper,
                                      UserCatalogPersistenceMapper userCatalogPersistenceMapper,
                                      UserCartPolicy userCartPolicy,
                                      UserCatalogPolicy userCatalogPolicy) {
        this.userCartPersistenceMapper = userCartPersistenceMapper;
        this.userCatalogPersistenceMapper = userCatalogPersistenceMapper;
        this.userCartPolicy = userCartPolicy;
        this.userCatalogPolicy = userCatalogPolicy;
    }

    @Transactional
    public void addItem(UserAuthenticatedUser authenticatedUser, UserCartItemAddRequest request) {
        Long productId = userCartPolicy.parseRequiredProductId(request.getProductId());
        Long batchId = userCartPolicy.parseOptionalBatchId(request.getBatchId());
        String productSpec = userCartPolicy.normalizeOptionalSpec(request.getProductSpec());
        int quantity = userCartPolicy.resolveQuantity(request.getQuantity());

        UserCartProductSelectionRow selection = batchId == null
                ? userCatalogPersistenceMapper.findRecommendedSellableBatchSelection(productId)
                : userCatalogPersistenceMapper.findSellableBatchSelection(productId, batchId);
        userCartPolicy.ensureSellableSelectionExists(selection != null);

        BigDecimal unitPrice = userCatalogPolicy.calculateCurrentPrice(
                selection.getListPrice(),
                userCatalogPolicy.calculateDaysToExpire(selection.getExpirationTime()),
                resolveDiscountRate(selection.getExpirationTime())
        );

        UserCartItemDataObject existing = userCartPersistenceMapper.findBySelection(
                authenticatedUser.getUserId(),
                productId,
                selection.getBatchId(),
                productSpec
        );
        int nextQuantity = existing == null ? quantity : existing.getNumber() + quantity;
        userCartPolicy.ensureQuantityWithinAvailable(nextQuantity, selection.getAvailableQuantity());

        if (existing == null) {
            UserCartItemDataObject cartItem = new UserCartItemDataObject();
            cartItem.setUserId(authenticatedUser.getUserId());
            cartItem.setProductId(productId);
            cartItem.setBatchId(selection.getBatchId());
            cartItem.setName(selection.getProductName());
            cartItem.setImage(selection.getImage());
            cartItem.setProductSpec(productSpec);
            cartItem.setNumber(quantity);
            cartItem.setAmount(unitPrice);
            cartItem.setCreateTime(LocalDateTime.now());
            userCartPersistenceMapper.insert(cartItem);
            return;
        }

        existing.setNumber(nextQuantity);
        existing.setAmount(unitPrice);
        userCartPersistenceMapper.updateQuantityById(existing);
    }

    public List<UserCartItemResponse> listItems(UserAuthenticatedUser authenticatedUser) {
        return userCartPersistenceMapper.listByUserId(authenticatedUser.getUserId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void updateItemQuantity(UserAuthenticatedUser authenticatedUser,
                                   String cartItemId,
                                   UserCartItemQuantityUpdateRequest request) {
        Long id = userCartPolicy.parseRequiredCartItemId(cartItemId);
        int quantity = userCartPolicy.resolveQuantity(request.getQuantity());

        UserCartItemRow existing = userCartPersistenceMapper.findRowByIdAndUserId(id, authenticatedUser.getUserId());
        userCartPolicy.ensureOwnedCartItemExists(existing != null);

        UserCartProductSelectionRow selection = userCatalogPersistenceMapper.findSellableBatchSelection(
                existing.getProductId(),
                existing.getBatchId()
        );
        userCartPolicy.ensureSellableSelectionExists(selection != null);
        userCartPolicy.ensureQuantityWithinAvailable(quantity, selection.getAvailableQuantity());

        UserCartItemDataObject update = new UserCartItemDataObject();
        update.setId(id);
        update.setNumber(quantity);
        update.setAmount(userCatalogPolicy.calculateCurrentPrice(
                selection.getListPrice(),
                userCatalogPolicy.calculateDaysToExpire(selection.getExpirationTime()),
                resolveDiscountRate(selection.getExpirationTime())
        ));
        userCartPersistenceMapper.updateQuantityById(update);
    }

    @Transactional
    public void removeItem(UserAuthenticatedUser authenticatedUser, String cartItemId) {
        Long id = userCartPolicy.parseRequiredCartItemId(cartItemId);
        int affectedRows = userCartPersistenceMapper.deleteByIdAndUserId(id, authenticatedUser.getUserId());
        userCartPolicy.ensureOwnedCartItemRemoved(affectedRows > 0);
    }

    @Transactional
    public void clear(UserAuthenticatedUser authenticatedUser) {
        userCartPersistenceMapper.clearByUserId(authenticatedUser.getUserId());
    }

    private UserCartItemResponse toResponse(UserCartItemRow row) {
        BigDecimal unitPrice = row.getAmount();
        BigDecimal lineAmount = unitPrice.multiply(BigDecimal.valueOf(row.getNumber()));
        return UserCartItemResponse.builder()
                .id(String.valueOf(row.getId()))
                .productId(String.valueOf(row.getProductId()))
                .batchId(row.getBatchId() == null ? null : String.valueOf(row.getBatchId()))
                .name(row.getName())
                .image(row.getImage())
                .productSpec(row.getProductSpec())
                .quantity(row.getNumber())
                .unitPrice(unitPrice)
                .lineAmount(lineAmount)
                .availableQuantity(row.getAvailableQuantity())
                .nearestExpiryDate(userCatalogPolicy.formatExpiryDate(row.getExpirationTime()))
                .build();
    }

    private BigDecimal resolveDiscountRate(LocalDateTime expirationTime) {
        Integer daysToExpire = userCatalogPolicy.calculateDaysToExpire(expirationTime);
        List<UserCatalogPricingRuleRow> pricingRules = userCatalogPersistenceMapper.listEnabledPricingRules();
        if (daysToExpire == null) {
            return BigDecimal.ONE;
        }
        for (UserCatalogPricingRuleRow pricingRule : pricingRules) {
            if (pricingRule.getMinDaysToExpire() != null
                    && pricingRule.getMaxDaysToExpire() != null
                    && daysToExpire >= pricingRule.getMinDaysToExpire()
                    && daysToExpire <= pricingRule.getMaxDaysToExpire()) {
                return pricingRule.getDiscountRate();
            }
        }
        return BigDecimal.ONE;
    }
}
