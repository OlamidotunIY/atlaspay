package core.shared;

public interface Specification<T> {
    boolean isSatisfiedBy(T candidate);         // true if candidate meets the rule

    Specification<T> and(Specification<T> other); // combinator: logical AND

    Specification<T> or(Specification<T> other);  // combinator: logical OR
}
