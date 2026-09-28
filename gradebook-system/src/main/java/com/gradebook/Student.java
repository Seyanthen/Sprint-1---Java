package com.gradebook;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A student and the grades recorded for that student.
 */
public class Student {
    private final String id;
    private String name;
    private final Map<String, Grade> grades;

    public Student(String id, String name) {
        this(id, name, Map.of());
    }

    public Student(String id, String name, Map<String, Grade> grades) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.grades = new LinkedHashMap<>();
        grades.forEach(this::putGrade);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    public Map<String, Grade> getGrades() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(grades));
    }

    public Grade getGrade(String assignmentId) {
        return grades.get(Objects.requireNonNull(assignmentId, "assignmentId"));
    }

    public void addGrade(Grade grade) {
        Objects.requireNonNull(grade, "grade");
        putGrade(grade.assignmentId(), grade);
    }

    public Grade removeGrade(String assignmentId) {
        return grades.remove(Objects.requireNonNull(assignmentId, "assignmentId"));
    }

    private void putGrade(String assignmentId, Grade grade) {
        Objects.requireNonNull(assignmentId, "assignmentId");
        Objects.requireNonNull(grade, "grade");
        if (!assignmentId.equals(grade.assignmentId())) {
            throw new IllegalArgumentException("Map key must match grade assignmentId");
        }
        grades.put(assignmentId, grade);
    }
}
