package com.runmvp.subscription.usecase;

import com.runmvp.subscription.application.port.in.VerifyGooglePlayPurchaseUseCase;
import com.runmvp.subscription.application.port.out.BillingEventRepository;
import com.runmvp.subscription.application.port.out.GooglePlaySubscriptionVerifier;
import com.runmvp.subscription.application.port.out.SubscriptionRepository;
import com.runmvp.subscription.domain.Subscription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerifyGooglePlayPurchaseUseCaseTest {

    @Mock SubscriptionRepository subscriptionRepository;
    @Mock BillingEventRepository billingEventRepository;
    @Mock GooglePlaySubscriptionVerifier verifier;

    VerifyGooglePlayPurchaseUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new VerifyGooglePlayPurchaseUseCaseImpl(
            subscriptionRepository, billingEventRepository, verifier);
    }

    @Test
    void execute_validActivePurchase_createsSubscriptionAndReturnsPremium() {
        Instant future = Instant.now().plus(30, ChronoUnit.DAYS);
        when(verifier.verify("token123", "monthly")).thenReturn(
            new GooglePlaySubscriptionVerifier.VerifiedSubscription(
                "SUBSCRIPTION_STATE_ACTIVE", "monthly", future));
        when(subscriptionRepository.findByPurchaseToken("token123")).thenReturn(Optional.empty());

        var result = useCase.execute(new VerifyGooglePlayPurchaseUseCase.Command(1L, "token123", "monthly"));

        assertThat(result.entitlement()).isEqualTo("PREMIUM_ACTIVE");
        ArgumentCaptor<Subscription> cap = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepository).save(cap.capture());
        assertThat(cap.getValue().getUserId()).isEqualTo(1L);
        assertThat(cap.getValue().getStatus()).isEqualTo(Subscription.Status.ACTIVE);
        verify(billingEventRepository).save(any());
    }

    @Test
    void execute_expiredPurchase_returnsFreeTier() {
        Instant past = Instant.now().minus(1, ChronoUnit.DAYS);
        when(verifier.verify(anyString(), anyString())).thenReturn(
            new GooglePlaySubscriptionVerifier.VerifiedSubscription(
                "SUBSCRIPTION_STATE_EXPIRED", "monthly", past));

        var result = useCase.execute(new VerifyGooglePlayPurchaseUseCase.Command(1L, "token-expired", "monthly"));

        assertThat(result.entitlement()).isEqualTo("FREE");
        verify(subscriptionRepository, never()).save(any());
        verify(billingEventRepository).save(any());
    }

    @Test
    void execute_existingSubscription_renewsExpiry() {
        Instant oldExpiry = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant newExpiry = Instant.now().plus(31, ChronoUnit.DAYS);
        Subscription existing = Subscription.reconstitute(
            42L, 1L, "token-renew", "monthly", Subscription.ProductType.MONTHLY,
            Subscription.Status.ACTIVE, oldExpiry, Instant.now(), Instant.now());

        when(verifier.verify("token-renew", "monthly")).thenReturn(
            new GooglePlaySubscriptionVerifier.VerifiedSubscription(
                "SUBSCRIPTION_STATE_ACTIVE", "monthly", newExpiry));
        when(subscriptionRepository.findByPurchaseToken("token-renew")).thenReturn(Optional.of(existing));

        var result = useCase.execute(new VerifyGooglePlayPurchaseUseCase.Command(1L, "token-renew", "monthly"));

        assertThat(result.entitlement()).isEqualTo("PREMIUM_ACTIVE");
        ArgumentCaptor<Subscription> cap = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepository).save(cap.capture());
        assertThat(cap.getValue().getId()).isEqualTo(42L);
        assertThat(cap.getValue().getExpiresAt()).isEqualTo(newExpiry);
    }
}
