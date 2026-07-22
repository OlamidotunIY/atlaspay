package core.shared;

import java.util.Optional;

public interface Repository<T, ID> {

    Optional<T> findById(ID id);
    T save(T aggregate);
    PageResult<T> findAll(int pageNumber, int pageSize);
}
