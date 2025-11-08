package Singheatlh.springboot_backend.validation;

/**
 * Generic interface for validation rules.
 * Following Chain of Responsibility pattern and Open/Closed Principle.
 * New validation rules can be added without modifying existing code.
 *
 * This provides a type-safe, reusable validation framework that can be
 * applied to any type of object (DTOs, requests, entities, etc.).
 *
 * @param <T> The type of object to validate
 */
public interface ValidationRule<T> {

    /**
     * Validate an object of type T.
     *
     * @param object The object to validate
     * @throws IllegalArgumentException if validation fails with descriptive message
     * @throws IllegalStateException if validation fails due to state issues
     */
    void validate(T object);
}
