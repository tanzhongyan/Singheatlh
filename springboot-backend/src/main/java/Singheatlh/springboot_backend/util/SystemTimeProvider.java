package Singheatlh.springboot_backend.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * System time provider implementation
 * Returns actual system time
 */
@Component
public class SystemTimeProvider implements TimeProvider {

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}
