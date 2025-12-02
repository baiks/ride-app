package ride.sharing.com.impl;

/**
 * Validation group marker interface for non-empty field validation.
 * Used to validate fields only when they are present in the request.
 *
 * Usage in DTOs:
 * @NotNull(message = "Field cannot be null", groups = {NonEmptyValidation.class})
 *
 * Usage in Controllers:
 * @Validated(NonEmptyValidation.class) @RequestBody UserUpdateDTO dto
 */
public interface NonEmptyValidation {
}
