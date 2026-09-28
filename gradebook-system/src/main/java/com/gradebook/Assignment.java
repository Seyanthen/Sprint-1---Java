package com.gradebook;

/**
 * An assignment that can receive grades.
 */
public record Assignment(
        String id,
        String title,
        double maxPoints,
        boolean isGroupAssignment) {
}
