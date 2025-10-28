package Singheatlh.springboot_backend.util;

import java.time.LocalDateTime;

/**
 * Abstraction for getting current time
 * Following Dependency Inversion Principle
 * Enables easier testing by allowing time to be mocked
 */
public interface TimeProvider {

    /**
     * Get the current date and time
     * @return Current LocalDateTime
     */
    LocalDateTime now();
}
