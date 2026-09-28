package com.gradebook;

/**
 * A student's score for an assignment.
 */
public record Grade(String assignmentId, double score) {
}
