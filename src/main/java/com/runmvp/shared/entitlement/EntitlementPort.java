package com.runmvp.shared.entitlement;

public interface EntitlementPort {
    Entitlement getEffectiveEntitlement(Long userId);

    enum Entitlement { FREE, PREMIUM_ACTIVE }
}
