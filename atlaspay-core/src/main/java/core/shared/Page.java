package core.shared;

import java.util.List;

public record Page<T>(List<T> content, int pageNumber, int pageSize, long totalElements) {
}
