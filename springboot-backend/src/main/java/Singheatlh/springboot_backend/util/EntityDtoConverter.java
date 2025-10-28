package Singheatlh.springboot_backend.util;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Generic utility for converting entity lists to DTO lists
 * Eliminates duplicate stream/map/collect code
 * Following DRY (Don't Repeat Yourself) principle
 */
@Component
public class EntityDtoConverter {

    /**
     * Convert a list of entities to a list of DTOs
     * @param entities List of entities
     * @param mapper Mapping function from entity to DTO
     * @param <E> Entity type
     * @param <D> DTO type
     * @return List of DTOs
     */
    public <E, D> List<D> convertList(List<E> entities, Function<E, D> mapper) {
        return entities.stream()
                .map(mapper)
                .collect(Collectors.toList());
    }
}
