package Singheatlh.springboot_backend.strategy.cancellation;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory for selecting the appropriate appointment cancellation strategy.
 * Following Factory Pattern and Strategy Pattern.
 * Uses the isStaff flag in CancellationContext to determine which strategy to use.
 *
 * The strategy map is cached at initialization for performance.
 */
@Component
@RequiredArgsConstructor
public class CancellationStrategyFactory {

    private final List<AppointmentCancellationStrategy> strategies;
    private Map<String, AppointmentCancellationStrategy> strategyMap;

    /**
     * Initialize the strategy map once during bean creation.
     * This avoids rebuilding the map on every request.
     */
    @PostConstruct
    public void initStrategyMap() {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        AppointmentCancellationStrategy::getStrategyName,
                        Function.identity()
                ));
    }

    /**
     * Get the appropriate cancellation strategy based on the context.
     * @param context The cancellation context containing user role information
     * @return The appropriate strategy (PatientCancellationStrategy or StaffCancellationStrategy)
     */
    public AppointmentCancellationStrategy getStrategy(CancellationContext context) {
        // Select strategy based on isStaff flag
        if (context.isStaff()) {
            return strategyMap.get("STAFF");
        } else {
            return strategyMap.get("PATIENT");
        }
    }
}
