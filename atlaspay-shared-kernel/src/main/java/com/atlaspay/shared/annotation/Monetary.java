package com.atlaspay.shared.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bean validation constraint to ensure a monetary value is valid 
 * (e.g., positive, not null, correctly scaled).
 * Implementation of the validator will be in the core app or shared module.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
public @interface Monetary {
    String message() default "Invalid monetary value";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    boolean allowZero() default false;
    boolean allowNegative() default false;
}
