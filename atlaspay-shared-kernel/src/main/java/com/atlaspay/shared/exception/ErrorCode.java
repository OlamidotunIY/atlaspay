package com.atlaspay.shared.exception;

/**
 * Interface implemented by module-specific error code Enums.
 * This allows each bounded context to define its own error codes
 * without coupling them into a single global Enum.
 */
public interface ErrorCode {
    /**
     * Returns the name of the enum constant (e.g., "USER_NOT_FOUND").
     * This method is provided automatically by Java Enums.
     */
    String name();
}
