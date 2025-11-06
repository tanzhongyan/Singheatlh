package Singheatlh.springboot_backend.strategy;

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
 */
@Component
@RequiredArgsConstructor
public class AppointmentStrategyFactory {

    private final List<AppointmentCreationStrategy> strategies;

    /**
     * Get the appropriate strategy based on the request.
     * @param request The appointment creation request
     * @return The appropriate strategy (WalkInAppointmentStrategy or RegularAppointmentStrategy)
     */
    public AppointmentCreationStrategy getStrategy(CreateAppointmentRequest request) {
        // Build a map of strategy name to strategy for efficient lookup
        Map<String, AppointmentCreationStrategy> strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        AppointmentCreationStrategy::getStrategyName,
                        Function.identity()
                ));

        // Select strategy based on isWalkIn flag
        if (Boolean.TRUE.equals(request.getIsWalkIn())) {
            return strategyMap.get("WALK_IN");
        } else {
            return strategyMap.get("REGULAR");
        }
    }
}
