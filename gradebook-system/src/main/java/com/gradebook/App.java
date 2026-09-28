package com.gradebook;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Small interactive test harness for the gradebook domain model and service.
 *
 * <p>Use the buttons to exercise individual grading, group grading, and
 * resetting the in-memory sample data.</p>
 */
public class App extends Application {
    private GradebookService gradebookService;
    private TextArea output;

    @Override
    public void start(Stage stage) {
        gradebookService = createSampleGradebook();

        Label instructions = new Label(
                "Use the buttons to test GradebookService operations on sample data.");
        output = new TextArea();
        output.setEditable(false);
        output.setPrefRowCount(12);
        output.setPrefColumnCount(60);

        Button individualGradeButton = new Button("Grade Alex: 92");
        individualGradeButton.setOnAction(event -> {
            gradebookService.gradeStudent("s1", "a1", 92.0);
            refreshOutput();
        });

        Button groupGradeButton = new Button("Grade Blue Group: 88");
        groupGradeButton.setOnAction(event -> {
            gradebookService.gradeGroup("g1", "a1", 88.0);
            refreshOutput();
        });

        Button resetButton = new Button("Reset Sample Data");
        resetButton.setOnAction(event -> {
            gradebookService = createSampleGradebook();
            refreshOutput();
        });

        HBox actions = new HBox(10, individualGradeButton, groupGradeButton, resetButton);
        VBox root = new VBox(10, instructions, actions, output);
        root.setPadding(new Insets(15));
        refreshOutput();

        Scene scene = new Scene(root, 700, 400);

        stage.setTitle("Gradebook System - Service Test Harness");
        stage.setScene(scene);
        stage.show();
    }

    /** Creates predictable sample data for manually testing the service. */
    private GradebookService createSampleGradebook() {
        GradebookService service = new GradebookService();
        service.addStudent(new Student("s1", "Alex"));
        service.addStudent(new Student("s2", "Sam"));
        service.addAssignment(new Assignment("a1", "Team Project", 100.0, true));

        Group blueGroup = new Group("g1", "Blue Group");
        service.addGroup(blueGroup);
        service.addStudentToGroup("g1", "s1");
        service.addStudentToGroup("g1", "s2");
        return service;
    }

    /** Renders the service state so each button action can be observed. */
    private void refreshOutput() {
        StringBuilder text = new StringBuilder();
        text.append("Students:\n");
        for (Student student : gradebookService.getStudents()) {
            text.append("  ").append(student.getId()).append(" - ")
                    .append(student.getName()).append(" ")
                    .append(student.getGrades()).append('\n');
        }

        text.append("\nAssignments:\n");
        for (Assignment assignment : gradebookService.getAssignments()) {
            text.append("  ").append(assignment.id()).append(" - ")
                    .append(assignment.title()).append(" (max ")
                    .append(assignment.maxPoints()).append(")\n");
        }

        text.append("\nGroups:\n");
        for (Group group : gradebookService.getGroups()) {
            text.append("  ").append(group.getGroupId()).append(" - ")
                    .append(group.getGroupName()).append(" members=")
                    .append(group.getStudentIds()).append('\n');
        }
        output.setText(text.toString());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
