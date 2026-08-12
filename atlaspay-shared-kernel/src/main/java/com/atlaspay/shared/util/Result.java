package com.atlaspay.shared.util;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A functional Result type for handling success and failure without throwing exceptions.
 */
public sealed interface Result<T, E> permits Result.Success, Result.Failure {

    static <T, E> Result<T, E> success(T value) {
        return new Success<>(value);
    }

    static <T, E> Result<T, E> failure(E error) {
        return new Failure<>(error);
    }

    boolean isSuccess();
    boolean isFailure();
    
    <U> Result<U, E> map(Function<? super T, ? extends U> mapper);
    
    <U> Result<U, E> flatMap(Function<? super T, ? extends Result<U, E>> mapper);
    
    T orElseThrow() throws RuntimeException;

    void ifSuccess(Consumer<? super T> action);
    void ifFailure(Consumer<? super E> action);

    record Success<T, E>(T value) implements Result<T, E> {
        @Override public boolean isSuccess() { return true; }
        @Override public boolean isFailure() { return false; }

        @Override
        public <U> Result<U, E> map(Function<? super T, ? extends U> mapper) {
            return new Success<>(mapper.apply(value));
        }

        @Override
        public <U> Result<U, E> flatMap(Function<? super T, ? extends Result<U, E>> mapper) {
            return mapper.apply(value);
        }

        @Override
        public T orElseThrow() {
            return value;
        }

        @Override
        public void ifSuccess(Consumer<? super T> action) {
            action.accept(value);
        }

        @Override
        public void ifFailure(Consumer<? super E> action) {
            // Do nothing
        }
    }

    record Failure<T, E>(E error) implements Result<T, E> {
        @Override public boolean isSuccess() { return false; }
        @Override public boolean isFailure() { return true; }

        @Override
        public <U> Result<U, E> map(Function<? super T, ? extends U> mapper) {
            return new Failure<>(error);
        }

        @Override
        public <U> Result<U, E> flatMap(Function<? super T, ? extends Result<U, E>> mapper) {
            return new Failure<>(error);
        }

        @Override
        public T orElseThrow() {
            if (error instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException(error.toString());
        }

        @Override
        public void ifSuccess(Consumer<? super T> action) {
            // Do nothing
        }

        @Override
        public void ifFailure(Consumer<? super E> action) {
            action.accept(error);
        }
    }
}
