package com.learning.database.employee.dto;

import java.util.List;
import java.util.Map;

/** Window<T> flattened for JSON: content + hasNext + cursor for the next request. */
public record WindowResponse<T>(List<T> content, boolean hasNext, Map<String, Object> nextCursor) {}
