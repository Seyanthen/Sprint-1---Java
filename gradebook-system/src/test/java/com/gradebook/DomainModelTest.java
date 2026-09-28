package com.gradebook;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class DomainModelTest {
    @Test
    public void recordsExposeTheirDeclaredComponents() {
        Assignment assignment = new Assignment("a1", "Quiz 1", 25.0, false);
        Grade grade = new Grade("a1", 23.5);

        assertEquals("a1", assignment.id());
        assertEquals("Quiz 1", assignment.title());
        assertEquals(25.0, assignment.maxPoints(), 0.0);
        assertFalse(assignment.isGroupAssignment());
        assertEquals("a1", grade.assignmentId());
        assertEquals(23.5, grade.score(), 0.0);
    }

    @Test
    public void studentEncapsulatesAndManagesGrades() {
        Student student = new Student("s1", "Alex", Map.of("a1", new Grade("a1", 18.0)));
        student.addGrade(new Grade("a2", 20.0));

        assertEquals("Alex", student.getName());
        assertEquals(18.0, student.getGrade("a1").score(), 0.0);
        assertEquals(2, student.getGrades().size());

        try {
            student.getGrades().put("a3", new Grade("a3", 10.0));
        } catch (UnsupportedOperationException expected) {
            return;
        }
        throw new AssertionError("Student grades must not be externally mutable");
    }

    @Test
    public void groupEncapsulatesAndManagesStudentIds() {
        Group group = new Group("g1", "Blue", Set.of("s1"));
        assertTrue(group.containsStudent("s1"));
        assertTrue(group.addStudent("s2"));
        assertEquals(Set.of("s1", "s2"), group.getStudentIds());
        assertTrue(group.removeStudent("s1"));
        assertFalse(group.containsStudent("s1"));

        try {
            group.getStudentIds().add("s3");
        } catch (UnsupportedOperationException expected) {
            return;
        }
        throw new AssertionError("Group student IDs must not be externally mutable");
    }
}
