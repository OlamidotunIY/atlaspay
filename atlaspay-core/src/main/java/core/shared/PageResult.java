package core.shared;

import java.util.List;

public record PageResult<T>(List<T> content, int pageNumber, int pageSize, long totalElements) {
}
