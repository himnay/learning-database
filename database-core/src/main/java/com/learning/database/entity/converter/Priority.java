package com.learning.database.entity.converter;

/**
 * Enum stored in the DB as a VARCHAR via PriorityConverter.
 * Without @Convert, JPA would store the ordinal (0,1,2) by default,
 * which is fragile — reordering enum constants breaks existing data.
 * Using an AttributeConverter stores a stable string instead.
 */
public enum Priority {
    LOW("low"),
    NORMAL("normal"),
    HIGH("high");

    private final String dbValue;

    Priority(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static Priority fromDbValue(String dbValue) {
        for (Priority p : values()) {
            if (p.dbValue.equalsIgnoreCase(dbValue)) return p;
        }
        throw new IllegalArgumentException("Unknown priority db value: " + dbValue);
    }
}
