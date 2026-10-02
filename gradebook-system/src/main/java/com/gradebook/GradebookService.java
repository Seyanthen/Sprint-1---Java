package com.gradebook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * In-memory service for managing gradebook entities and grades.
 *
 * <p>The service owns the lists of students, assignments, and groups. Callers
 * receive read-only list views so changes must go through this service.</p>
 */
public class GradebookService {
    private final List<Student> students = new ArrayList<>();
    private final List<Assignment> assignments = new ArrayList<>();
    private final List<Group> groups = new ArrayList<>();

    /** Returns a live, unmodifiable view of the registered students. */
    public List<Student> getStudents() {
        return Collections.unmodifiableList(students);
    }

    /** Returns a live, unmodifiable view of the registered assignments. */
    public List<Assignment> getAssignments() {
        return Collections.unmodifiableList(assignments);
    }

    /** Returns a live, unmodifiable view of the registered groups. */
    public List<Group> getGroups() {
        return Collections.unmodifiableList(groups);
    }

    public void addStudent(Student student) {
        Objects.requireNonNull(student, "student");
        // IDs are the stable keys used by groups and grades, so they must be unique.
        if (findStudent(student.getId()) != null) {
            throw new IllegalArgumentException("Student ID already exists: " + student.getId());
        }
        students.add(student);
    }

    /** Removes a student and clears that student's ID from every group. */
    public boolean removeStudent(String studentId) {
        Student student = requireStudent(studentId);
        // Remove the ID from groups before removing the student itself.
        groups.forEach(group -> group.removeStudent(student.getId()));
        return students.remove(student);
    }

    public void addAssignment(Assignment assignment) {
        Objects.requireNonNull(assignment, "assignment");
        if (findAssignment(assignment.id()) != null) {
            throw new IllegalArgumentException("Assignment ID already exists: " + assignment.id());
        }
        assignments.add(assignment);
    }

    /** Removes an assignment and its grades from all students. */
    public boolean removeAssignment(String assignmentId) {
        Assignment assignment = requireAssignment(assignmentId);
        students.forEach(student -> student.removeGrade(assignment.id()));
        return assignments.remove(assignment);
    }

    public void addGroup(Group group) {
        Objects.requireNonNull(group, "group");
        if (findGroup(group.getGroupId()) != null) {
            throw new IllegalArgumentException("Group ID already exists: " + group.getGroupId());
        }
        groups.add(group);
    }

    public boolean removeGroup(String groupId) {
        return groups.remove(requireGroup(groupId));
    }

    /** Adds an existing student ID to an existing group. */
    public boolean addStudentToGroup(String groupId, String studentId) {
        requireStudent(studentId);
        return requireGroup(groupId).addStudent(studentId);
    }

    /** Removes a student ID from an existing group. */
    public boolean removeStudentFromGroup(String groupId, String studentId) {
        return requireGroup(groupId).removeStudent(studentId);
    }

    /** Records one student's score for an existing assignment. */
    public Grade gradeStudent(String studentId, String assignmentId, double score) {
        Student student = requireStudent(studentId);
        Assignment assignment = requireAssignment(assignmentId);
        // Student.addGrade replaces an older grade for the same assignment.
        Grade grade = new Grade(assignment.id(), score);
        student.addGrade(grade);
        return grade;
    }

    /** Calculates the average of all scores recorded for one student. */
    public double calculateStudentAverage(String studentId) {
        return requireStudent(studentId).getGrades().values().stream()
                .mapToDouble(Grade::score)
                .average()
                .orElse(0.0);
    }

    /** Calculates the average score recorded by the class for one assignment. */
    public double calculateAssignmentAverage(String assignmentId) {
        requireAssignment(assignmentId);
        return students.stream()
                .map(Student::getGrades)
                .map(grades -> grades.get(assignmentId))
                .filter(Objects::nonNull)
                .mapToDouble(Grade::score)
                .average()
                .orElse(0.0);
    }

    /**
     * Gives every member of a group the same score for an assignment.
     *
     * <p>All member IDs are resolved before any grade is written, so a missing
     * student cannot leave the group partially graded.</p>
     *
     * @return an unmodifiable map from student ID to the grade recorded
     */
    public Map<String, Grade> gradeGroup(String groupId, String assignmentId, double score) {
        Group group = requireGroup(groupId);
        Assignment assignment = requireAssignment(assignmentId);
        Map<String, Student> members = new LinkedHashMap<>();

        // Resolve every member first so a missing ID cannot cause a partial update.
        for (String studentId : group.getStudentIds()) {
            members.put(studentId, requireStudent(studentId));
        }

        Map<String, Grade> recordedGrades = new LinkedHashMap<>();
        // Write grades only after all group members have been validated.
        for (Map.Entry<String, Student> member : members.entrySet()) {
            Grade grade = new Grade(assignment.id(), score);
            member.getValue().addGrade(grade);
            recordedGrades.put(member.getKey(), grade);
        }
        return Collections.unmodifiableMap(recordedGrades);
    }

    private Student requireStudent(String studentId) {
        Objects.requireNonNull(studentId, "studentId");
        Student student = findStudent(studentId);
        if (student == null) {
            throw new IllegalArgumentException("Unknown student ID: " + studentId);
        }
        return student;
    }

    private Assignment requireAssignment(String assignmentId) {
        Objects.requireNonNull(assignmentId, "assignmentId");
        Assignment assignment = findAssignment(assignmentId);
        if (assignment == null) {
            throw new IllegalArgumentException("Unknown assignment ID: " + assignmentId);
        }
        return assignment;
    }

    private Group requireGroup(String groupId) {
        Objects.requireNonNull(groupId, "groupId");
        Group group = findGroup(groupId);
        if (group == null) {
            throw new IllegalArgumentException("Unknown group ID: " + groupId);
        }
        return group;
    }

    private Student findStudent(String studentId) {
        return students.stream()
                .filter(student -> student.getId().equals(studentId))
                .findFirst()
                .orElse(null);
    }

    private Assignment findAssignment(String assignmentId) {
        return assignments.stream()
                .filter(assignment -> assignment.id().equals(assignmentId))
                .findFirst()
                .orElse(null);
    }

    private Group findGroup(String groupId) {
        return groups.stream()
                .filter(group -> group.getGroupId().equals(groupId))
                .findFirst()
                .orElse(null);
    }
}
