package com.runmvp.subscription.adapter.in.web;

import com.runmvp.shared.security.AuthenticatedUser;
import com.runmvp.subscription.application.port.in.VerifyGooglePlayPurchaseUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/subscriptions")
public class SubscriptionController {

    private final VerifyGooglePlayPurchaseUseCase verifyUseCase;

    public SubscriptionController(VerifyGooglePlayPurchaseUseCase verifyUseCase) {
        this.verifyUseCase = verifyUseCase;
    }

    record VerifyRequest(String purchaseToken, String productId) {}
    record VerifyResponse(String entitlement) {}

    @PostMapping("/verify")
    public ResponseEntity<VerifyResponse> verify(
            @AuthenticationPrincipal AuthenticatedUser p,
            @RequestBody VerifyRequest request) {
        var result = verifyUseCase.execute(new VerifyGooglePlayPurchaseUseCase.Command(
            p.userId(), request.purchaseToken(), request.productId()));
        return ResponseEntity.ok(new VerifyResponse(result.entitlement()));
    }
}
