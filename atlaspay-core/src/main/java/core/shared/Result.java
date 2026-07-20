package core.shared;

public sealed interface Result<T, E> permits Result.Ok, Result.Err {
    record Ok<T, E>(T value) implements Result<T, E> {
        @Override
        public boolean isOk() {
            return true;
        }

        @Override
        public T orElseThrow() {
            return value;
        }
    }
    record Err<T, E>(E error) implements Result<T, E> {
        @Override
        public boolean isOk() {
            return false;
        }

        @Override
        public T orElseThrow() {
            return null;
        }
    }

    boolean isOk();
    T orElseThrow();
}
