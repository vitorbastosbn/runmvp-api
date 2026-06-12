package com.runmvp.subscription.application.port.in;

public interface VerifyGooglePlayPurchaseUseCase {
    record Command(Long userId, String purchaseToken, String productId) {}
    record Result(String entitlement) {}
    Result execute(Command command);
}
