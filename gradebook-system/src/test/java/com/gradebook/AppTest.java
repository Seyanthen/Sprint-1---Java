package com.gradebook;

import static org.junit.Assert.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;

/**
 * Tests the persistence workflow used by the application.
 */
public class AppTest 
{
    @Test
    public void savesAndLoadsTheCompleteGradebook() throws Exception {
        GradebookService original = new GradebookService();
        original.addStudent(new Student("s1", "Alex"));
        original.addAssignment(new Assignment("a1", "Project", 100.0, true));
        original.addGroup(new Group("g1", "Blue"));
        original.addStudentToGroup("g1", "s1");
        original.gradeStudent("s1", "a1", 92.0);

        Path path = Files.createTempFile("gradebook-test-", ".json");
        try {
            PersistenceService persistence = new PersistenceService();
            persistence.saveGradebook(path, original);
            GradebookService restored = persistence.loadGradebook(path);

            assertEquals(original.getStudents().size(), restored.getStudents().size());
            assertEquals(original.getAssignments(), restored.getAssignments());
            assertEquals(original.getGroups().get(0).getStudentIds(),
                    restored.getGroups().get(0).getStudentIds());
            assertEquals(92.0, restored.getStudents().get(0).getGrade("a1").score(), 0.0);
        } finally {
            Files.deleteIfExists(path);
        }
    }
}
