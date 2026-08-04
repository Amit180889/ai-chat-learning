package com.amit.ai.chat.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of UserContextService with sample user seeded.
 */
@Component
public class InMemoryUserContextService implements UserContextService {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryUserContextService.class);

    private final Map<String, UserContext> store = new ConcurrentHashMap<>();

    public InMemoryUserContextService() {
        // Seed with sample user
        UserContext sampleUser = new UserContext(
                "amit",
                "en",
                "intermediate",
                "concise",
                "Software Engineer",
                List.of("Java", "Spring Boot", "LangChain4j", "AI")
        );
        store.put(sampleUser.userId(), sampleUser);
        logger.info("Seeded sample user: {}", sampleUser.userId());
    }

    @Override
    public Optional<UserContext> getUserContext(String userId) {
        if (userId == null || userId.isBlank()) return Optional.empty();
        return Optional.ofNullable(store.get(userId));
    }

    /**
     * Method to add users programmatically (for testing/setup).
     */
    public void addUser(UserContext userContext) {
        if (userContext != null && userContext.userId() != null) {
            store.put(userContext.userId(), userContext);
            logger.info("Added user: {}", userContext.userId());
        }
    }
}
