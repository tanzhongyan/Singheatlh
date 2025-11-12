package Singheatlh.springboot_backend.strategy;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import Singheatlh.springboot_backend.dto.CreateAppointmentRequest;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory for selecting the appropriate appointment creation strategy.
 * Following Factory Pattern and Strategy Pattern.
 * Uses the isWalkIn flag to determine which strategy to use.
 *
 * The strategy map is cached at initialization for performance.
 */
@Component
@RequiredArgsConstructor
public class AppointmentStrategyFactory {

    private final List<AppointmentCreationStrategy> strategies;
    private Map<String, AppointmentCreationStrategy> strategyMap;

    /**
     * Initialize the strategy map once during bean creation.
     * This avoids rebuilding the map on every request.
     */
    @PostConstruct
    public void initStrategyMap() {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        AppointmentCreationStrategy::getStrategyName,
                        Function.identity()
                ));
    }

    /**
     * Get the appropriate strategy based on the request.
     * @param request The appointment creation request
     * @return The appropriate strategy (WalkInAppointmentStrategy or RegularAppointmentStrategy)
     */
    public AppointmentCreationStrategy getStrategy(CreateAppointmentRequest request) {
        // Select strategy based on isWalkIn flag (primitive boolean defaults to false)
        if (request.isWalkIn()) {
            return strategyMap.get("WALK_IN");
        } else {
            return strategyMap.get("REGULAR");
        }
    }
}
