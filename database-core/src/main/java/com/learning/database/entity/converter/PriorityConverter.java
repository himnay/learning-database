package com.learning.database.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * @Converter(autoApply = true) — Hibernate automatically applies this converter
 * to every entity field of type Priority without needing @Convert on each field.
 *
 * Without autoApply, you annotate each field explicitly:
 *   @Convert(converter = PriorityConverter.class)
 *   private Priority priority;
 *
 * convertToDatabaseColumn : Java → DB  (Priority.HIGH → "high")
 * convertToEntityAttribute : DB → Java  ("high" → Priority.HIGH)
 */
@Converter(autoApply = true)
public class PriorityConverter implements AttributeConverter<Priority, String> {

    @Override
    public String convertToDatabaseColumn(Priority attribute) {
        return attribute == null ? null : attribute.getDbValue();
    }

    @Override
    public Priority convertToEntityAttribute(String dbData) {
        return dbData == null ? null : Priority.fromDbValue(dbData);
    }
}
