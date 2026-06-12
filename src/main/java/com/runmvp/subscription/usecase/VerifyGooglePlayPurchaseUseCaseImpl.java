package com.runmvp.subscription.usecase;

import com.runmvp.subscription.application.port.in.VerifyGooglePlayPurchaseUseCase;
import com.runmvp.subscription.application.port.out.BillingEventRepository;
import com.runmvp.subscription.application.port.out.GooglePlaySubscriptionVerifier;
import com.runmvp.subscription.application.port.out.SubscriptionRepository;
import com.runmvp.subscription.domain.Subscription;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class VerifyGooglePlayPurchaseUseCaseImpl implements VerifyGooglePlayPurchaseUseCase {

    private final SubscriptionRepository subscriptionRepository;
    private final BillingEventRepository billingEventRepository;
    private final GooglePlaySubscriptionVerifier verifier;

    public VerifyGooglePlayPurchaseUseCaseImpl(
            SubscriptionRepository subscriptionRepository,
            BillingEventRepository billingEventRepository,
            GooglePlaySubscriptionVerifier verifier) {
        this.subscriptionRepository = subscriptionRepository;
        this.billingEventRepository = billingEventRepository;
        this.verifier = verifier;
    }

    @Override
    @Transactional
    public Result execute(Command command) {
        GooglePlaySubscriptionVerifier.VerifiedSubscription verified =
            verifier.verify(command.purchaseToken(), command.productId());

        billingEventRepository.save(new BillingEventRepository.Event(
            command.purchaseToken(), "VERIFY_REQUEST", "{}", Instant.now()));

        if (!verified.isEntitled()) {
            return new Result("FREE");
        }

        Optional<Subscription> existing = subscriptionRepository
            .findByPurchaseToken(command.purchaseToken());

        Subscription sub;
        if (existing.isPresent()) {
            sub = existing.get();
            sub.renew(verified.expiryTime());
        } else {
            Subscription.ProductType type = command.productId().contains("annual")
                ? Subscription.ProductType.ANNUAL : Subscription.ProductType.MONTHLY;
            sub = Subscription.create(command.userId(), command.purchaseToken(),
                command.productId(), type, verified.expiryTime());
        }
        subscriptionRepository.save(sub);
        return new Result("PREMIUM_ACTIVE");
    }
}
