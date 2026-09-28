package com.gradebook;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A group of students identified by their student IDs.
 */
public class Group {
    private final String groupId;
    private String groupName;
    private final Set<String> studentIds;

    public Group(String groupId, String groupName) {
        this(groupId, groupName, Set.of());
    }

    public Group(String groupId, String groupName, Set<String> studentIds) {
        this.groupId = Objects.requireNonNull(groupId, "groupId");
        this.groupName = Objects.requireNonNull(groupName, "groupName");
        this.studentIds = new LinkedHashSet<>();
        studentIds.forEach(this::addStudent);
    }

    public String getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = Objects.requireNonNull(groupName, "groupName");
    }

    public Set<String> getStudentIds() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(studentIds));
    }

    public boolean addStudent(String studentId) {
        return studentIds.add(Objects.requireNonNull(studentId, "studentId"));
    }

    public boolean removeStudent(String studentId) {
        return studentIds.remove(Objects.requireNonNull(studentId, "studentId"));
    }

    public boolean containsStudent(String studentId) {
        return studentIds.contains(Objects.requireNonNull(studentId, "studentId"));
    }
}
