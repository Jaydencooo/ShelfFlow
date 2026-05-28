package com.shelfflow.services.user.auth.service;

public interface VerificationCodeDeliveryService {

    void deliver(String target, String purpose, String code, int expiresInSeconds);
}
