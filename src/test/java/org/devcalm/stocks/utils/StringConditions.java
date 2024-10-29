package org.devcalm.stocks.utils;

import org.assertj.core.api.Condition;

import java.util.UUID;

public class StringConditions {

    public static Condition<String> validUUID() {
        return new Condition<>(StringConditions::isValidUUID, "a valid UUID");
    }

    private static boolean isValidUUID(String uuid) {
        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
