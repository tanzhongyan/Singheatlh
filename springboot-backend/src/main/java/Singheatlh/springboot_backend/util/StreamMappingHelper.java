package Singheatlh.springboot_backend.util;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility class for common stream mapping operations.
 * Provides reusable mapping logic to reduce boilerplate code in service implementations.
 *
 * This eliminates the repetitive .stream().map(...).collect(Collectors.toList()) pattern
 * that appears throughout the service layer.
 */
public class StreamMappingHelper {

    private StreamMappingHelper() {
        // Private constructor to prevent instantiation of utility class
    }

    /**
     * Maps a list of entities to a list of DTOs using the provided mapper function.
     *
     * @param <E> The entity type
     * @param <D> The DTO type
     * @param entities The list of entities to map
     * @param mapper The mapping function (typically mapper::toDto)
     * @return List of mapped DTOs
     */
    public static <E, D> List<D> mapToList(List<E> entities, Function<E, D> mapper) {
        return entities.stream()
                .map(mapper)
                .collect(Collectors.toList());
    }

    /**
     * Maps a list of entities to a list of DTOs using the provided mapper function.
     * This is an alias for mapToList for better readability in some contexts.
     *
     * @param <E> The entity type
     * @param <D> The DTO type
     * @param entities The list of entities to map
     * @param mapper The mapping function (typically mapper::toDto)
     * @return List of mapped DTOs
     */
    public static <E, D> List<D> mapToDtoList(List<E> entities, Function<E, D> mapper) {
        return mapToList(entities, mapper);
    }
}
