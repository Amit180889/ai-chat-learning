package com.amit.ai.chat.user;

import java.util.Optional;

/**
 * Contract for managing user context.
 */
public interface UserContextService {

    Optional<UserContext> getUserContext(String userId);
}
