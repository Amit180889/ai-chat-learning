package com.amit.ai.chat.user;

import java.util.List;

/**
 * Immutable domain model representing user context.
 */
public record UserContext(
        String userId,
        String preferredLanguage,
        String experienceLevel,
        String preferredResponseStyle,
        String profession,
        List<String> interests
) {
}
