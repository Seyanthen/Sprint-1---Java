package com.gradebook;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

public class GradebookServiceTest {
    @Test
    public void managesEntitiesAndReturnsUnmodifiableViews() {
        GradebookService service = new GradebookService();
        Student student = new Student("s1", "Alex");
        Assignment assignment = new Assignment("a1", "Quiz 1", 25.0, false);
        Group group = new Group("g1", "Blue");

        service.addStudent(student);
        service.addAssignment(assignment);
        service.addGroup(group);
        service.addStudentToGroup("g1", "s1");

        assertEquals(1, service.getStudents().size());
        assertEquals(1, service.getAssignments().size());
        assertEquals(1, service.getGroups().size());

        try {
            service.getStudents().clear();
        } catch (UnsupportedOperationException expected) {
            return;
        }
        throw new AssertionError("Service collections must be unmodifiable");
    }

    @Test
    public void gradesIndividualStudentsAndEntireGroups() {
        GradebookService service = new GradebookService();
        service.addStudent(new Student("s1", "Alex"));
        service.addStudent(new Student("s2", "Sam"));
        service.addAssignment(new Assignment("a1", "Project", 100.0, true));
        service.addGroup(new Group("g1", "Blue"));
        service.addStudentToGroup("g1", "s1");
        service.addStudentToGroup("g1", "s2");

        service.gradeStudent("s1", "a1", 91.0);
        Map<String, Grade> groupGrades = service.gradeGroup("g1", "a1", 88.0);

        assertEquals(2, groupGrades.size());
        assertEquals(88.0, service.getStudents().get(0).getGrade("a1").score(), 0.0);
        assertEquals(88.0, service.getStudents().get(1).getGrade("a1").score(), 0.0);
        assertTrue(groupGrades.containsKey("s1"));
        assertTrue(groupGrades.containsKey("s2"));

        try {
            groupGrades.clear();
        } catch (UnsupportedOperationException expected) {
            return;
        }
        throw new AssertionError("Group grade results must be unmodifiable");
    }

    @Test
    public void removingEntitiesCleansRelatedData() {
        GradebookService service = new GradebookService();
        service.addStudent(new Student("s1", "Alex"));
        service.addAssignment(new Assignment("a1", "Quiz", 10.0, false));
        service.addGroup(new Group("g1", "Blue"));
        service.addStudentToGroup("g1", "s1");
        service.gradeStudent("s1", "a1", 9.0);

        service.removeStudent("s1");
        assertTrue(service.getGroups().get(0).getStudentIds().isEmpty());
        service.removeAssignment("a1");
        assertTrue(service.getAssignments().isEmpty());
    }
}
