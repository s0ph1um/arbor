package com.sophium.treeier.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.ClaimAccessor;

import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthUtil {
    public static Optional<ClaimAccessor> getAuthenticatedUserClaims() {
        return Optional.ofNullable(SecurityContextHolder.getContext())
            .map(SecurityContext::getAuthentication)
            .map(Authentication::getPrincipal)
            .filter(ClaimAccessor.class::isInstance)
            .map(ClaimAccessor.class::cast);
    }

    public static String getAuthenticatedUserEmail() {
        return getAuthenticatedUserClaims()
            .map(claims -> claims.getClaimAsString("email"))
            .orElse(null);
    }

    public static String getAuthenticatedUserName() {
        return getAuthenticatedUserClaims()
            .map(claims -> claims.getClaimAsString("name"))
            .orElse(null);
    }
}
