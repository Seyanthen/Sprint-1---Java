package com.gradebook;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/** JavaFX application for viewing and saving a gradebook. */
public class App extends Application {
    private final PersistenceService persistenceService = new PersistenceService();
    private final ListView<Student> studentList = new ListView<>();
    private final ListView<Assignment> assignmentList = new ListView<>();
    private final ListView<Group> groupList = new ListView<>();
    private final TextField studentIdField = new TextField();
    private final TextField studentNameField = new TextField();
    private final TableView<Grade> gradesTable = new TableView<>();
    private final TextField newAssignmentTitleField = new TextField();
    private final TextField newAssignmentMaxPointsField = new TextField();
    private final CheckBox newAssignmentGroupCheckBox = new CheckBox("Group assignment");
    private final TextField scoreField = new TextField();
    private final Label scoreEditorLabel = new Label("Select a student and assignment to edit a score.");
    private final Label studentAverageLabel = new Label("Average: —");
    private final Label assignmentAverageLabel = new Label("Class average: —");
    private final Label statusLabel = new Label("Sample gradebook loaded.");
    private GradebookService gradebookService;
    private Student selectedStudent;
    private Assignment selectedAssignment;

    /** Builds the toolbar and three-panel gradebook view. */
    @Override
    public void start(Stage stage) {
        gradebookService = createSampleGradebook();
        configureListCells();
        configureStudentEditor();
        refreshLists();
        studentList.getSelectionModel().selectedItemProperty()
                .addListener((observable, previous, current) -> populateStudentEditor(current));
        assignmentList.getSelectionModel().selectedItemProperty()
                .addListener((observable, previous, current) -> {
                    updateAssignmentDetails(current);
                    updateScoreEditor();
                });

        Button importButton = new Button("Import Gradebook");
        importButton.setOnAction(event -> importGradebook(stage));

        Button saveButton = new Button("Save Gradebook");
        saveButton.setOnAction(event -> saveGradebook(stage));

        ToolBar toolbar = new ToolBar(importButton, saveButton);
        HBox panels = new HBox(12,
                createPanel("Students", studentList),
                createPanel("Assignments", assignmentList),
                createPanel("Groups", groupList));
        HBox detailPanels = new HBox(12, createStudentEditor(), createAssignmentDetails());
        VBox bottomPanel = new VBox(8, detailPanels, statusLabel);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));
        root.setTop(toolbar);
        root.setCenter(panels);
        root.setBottom(bottomPanel);

        Scene scene = new Scene(root, 1000, 600);
        stage.setTitle("Gradebook System");
        stage.setScene(scene);
        stage.show();
    }

    /** Configures the table that displays grades for the selected student. */
    private void configureStudentEditor() {
        studentIdField.setPromptText("Student ID");
        studentIdField.setEditable(false);
        studentNameField.setPromptText("Student name");

        TableColumn<Grade, String> assignmentColumn = new TableColumn<>("Assignment");
        assignmentColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().assignmentId()));
        TableColumn<Grade, String> gradeColumn = new TableColumn<>("Grade");
        gradeColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(Double.toString(cell.getValue().score())));
        TableColumn<Grade, String> maxScoreColumn = new TableColumn<>("Max Score");
        maxScoreColumn.setCellValueFactory(cell -> {
            String assignmentId = cell.getValue().assignmentId();
            return new SimpleStringProperty(gradebookService.getAssignments().stream()
                    .filter(assignment -> assignment.id().equals(assignmentId))
                    .map(assignment -> Double.toString(assignment.maxPoints()))
                    .findFirst()
                    .orElse(""));
        });
        gradesTable.getColumns().setAll(List.of(assignmentColumn, gradeColumn, maxScoreColumn));
        gradesTable.setPlaceholder(new Label("Select a student to view grades."));
        gradesTable.setPrefHeight(120);
    }

    /** Creates the editable bottom panel for the currently selected student. */
    private VBox createStudentEditor() {
        Button saveChangesButton = new Button("Save Changes");
        saveChangesButton.setOnAction(event -> saveStudentChanges());
        GridPane fields = new GridPane();
        fields.setHgap(8);
        fields.setVgap(4);
        fields.add(new Label("Student ID:"), 0, 0);
        fields.add(new Label("Name:"), 1, 0);
        fields.add(studentIdField, 0, 1);
        fields.add(studentNameField, 1, 1);
        fields.add(saveChangesButton, 2, 1);
        VBox editor = new VBox(6, new Label("Selected Student"), fields,
                studentAverageLabel, gradesTable);
        HBox.setHgrow(editor, Priority.ALWAYS);
        return editor;
    }

    /** Creates the detail panel for the selected assignment. */
    private VBox createAssignmentDetails() {
        Button saveScoreButton = new Button("Save Score");
        saveScoreButton.setOnAction(event -> saveStudentScore());
        scoreField.setPromptText("Score");

        VBox details = new VBox(6,
                new Label("Selected Assignment"), assignmentAverageLabel,
                new Label("Edit Selected Student's Score"), scoreEditorLabel,
                new HBox(8, new Label("Grade:"), scoreField, saveScoreButton),
                new Separator(),
                createAssignmentForm());
        HBox.setHgrow(details, Priority.ALWAYS);
        return details;
    }

    /** Creates controls for adding a new individual or group assignment. */
    private VBox createAssignmentForm() {
        newAssignmentTitleField.setPromptText("Title");
        newAssignmentMaxPointsField.setPromptText("Max points");
        Button addAssignmentButton = new Button("Add Assignment");
        addAssignmentButton.setOnAction(event -> addAssignment());

        HBox firstRow = new HBox(8, new Label("New Assignment Title:"),
                newAssignmentTitleField);
        HBox secondRow = new HBox(8, new Label("Max points:"),
                newAssignmentMaxPointsField, newAssignmentGroupCheckBox,
                addAssignmentButton);
        return new VBox(6, firstRow, secondRow);
    }

    /** Populates or clears the editor when the student selection changes. */
    private void populateStudentEditor(Student student) {
        selectedStudent = student;
        if (student == null) {
            studentIdField.clear();
            studentNameField.clear();
            studentAverageLabel.setText("Average: —");
            gradesTable.setItems(FXCollections.observableArrayList());
            updateScoreEditor();
            return;
        }

        studentIdField.setText(student.getId());
        studentNameField.setText(student.getName());
        studentAverageLabel.setText(formatAverage("Average", 
                gradebookService.calculateStudentAverage(student.getId())));
        gradesTable.setItems(FXCollections.observableArrayList(student.getGrades().values()));
        updateScoreEditor();
    }

    /** Shows the score currently recorded for the selected student and assignment. */
    private void updateScoreEditor() {
        if (selectedStudent == null || selectedAssignment == null) {
            scoreEditorLabel.setText("Select a student and assignment to edit a score.");
            scoreField.clear();
            return;
        }
        scoreEditorLabel.setText(selectedStudent.getName() + " / " + selectedAssignment.title());
        Grade grade = selectedStudent.getGrade(selectedAssignment.id());
        if (grade == null) {
            scoreField.clear();
            scoreField.setPromptText("No score yet");
        } else {
            scoreField.setText(Double.toString(grade.score()));
        }
    }

    /** Validates and saves a score for the selected student and assignment. */
    private void saveStudentScore() {
        if (selectedStudent == null || selectedAssignment == null) {
            statusLabel.setText("Select both a student and assignment first.");
            return;
        }
        double score;
        try {
            score = Double.parseDouble(scoreField.getText().trim());
        } catch (NumberFormatException exception) {
            statusLabel.setText("Score must be a number.");
            return;
        }
        if (!Double.isFinite(score) || score < 0.0 || score > selectedAssignment.maxPoints()) {
            statusLabel.setText("Score must be between 0 and "
                    + selectedAssignment.maxPoints() + ".");
            return;
        }

        gradebookService.gradeStudent(selectedStudent.getId(), selectedAssignment.id(), score);
        populateStudentEditor(selectedStudent);
        updateAssignmentDetails(selectedAssignment);
        statusLabel.setText("Saved " + selectedAssignment.id() + " for " + selectedStudent.getId() + ".");
    }

    /** Validates and adds the assignment entered in the assignment form. */
    private void addAssignment() {
        String title = newAssignmentTitleField.getText().trim();
        double maxPoints;
        try {
            maxPoints = Double.parseDouble(newAssignmentMaxPointsField.getText().trim());
        } catch (NumberFormatException exception) {
            statusLabel.setText("Maximum points must be a number.");
            return;
        }
        if (title.isEmpty()) {
            statusLabel.setText("Assignment title is required.");
            return;
        }
        if (!Double.isFinite(maxPoints) || maxPoints <= 0.0) {
            statusLabel.setText("Maximum points must be greater than zero.");
            return;
        }

        String id = nextAssignmentId();
        try {
            gradebookService.addAssignment(new Assignment(id, title, maxPoints,
                    newAssignmentGroupCheckBox.isSelected()));
        } catch (IllegalArgumentException exception) {
            statusLabel.setText(exception.getMessage());
            return;
        }

        clearAssignmentForm();
        refreshLists();
        assignmentList.getItems().stream()
                .filter(assignment -> assignment.id().equals(id))
                .findFirst()
                .ifPresent(assignment -> assignmentList.getSelectionModel().select(assignment));
        statusLabel.setText("Added assignment " + id + ".");
    }

    /** Clears the new-assignment form after a successful add. */
    private void clearAssignmentForm() {
        newAssignmentTitleField.clear();
        newAssignmentMaxPointsField.clear();
        newAssignmentGroupCheckBox.setSelected(false);
    }

    /** Returns the next sequential assignment ID in the a1, a2, a3 format. */
    private String nextAssignmentId() {
        long highestId = gradebookService.getAssignments().stream()
                .map(Assignment::id)
                .filter(id -> id.matches("a\\d+"))
                .mapToLong(id -> {
                    try {
                        return Long.parseLong(id.substring(1));
                    } catch (NumberFormatException exception) {
                        return 0L;
                    }
                })
                .max()
                .orElse(0L);
        return "a" + (highestId + 1);
    }

    /** Updates the assignment detail panel when an assignment is selected. */
    private void updateAssignmentDetails(Assignment assignment) {
        selectedAssignment = assignment;
        if (assignment == null) {
            assignmentAverageLabel.setText("Class average: —");
            return;
        }
        assignmentAverageLabel.setText(formatAverage("Class average",
                gradebookService.calculateAssignmentAverage(assignment.id())));
    }

    /** Formats a statistic consistently in the detail panels. */
    private String formatAverage(String label, double average) {
        return String.format("%s: %.2f", label, average);
    }

    /** Saves the edited name for the selected student and refreshes the list. */
    private void saveStudentChanges() {
        if (selectedStudent == null) {
            statusLabel.setText("Select a student before saving changes.");
            return;
        }
        String name = studentNameField.getText().trim();
        if (name.isEmpty()) {
            statusLabel.setText("Student name cannot be empty.");
            return;
        }

        selectedStudent.setName(name);
        refreshLists();
        statusLabel.setText("Changes saved for " + selectedStudent.getId() + ".");
    }

    /** Creates one labeled panel for a gradebook collection. */
    private VBox createPanel(String title, ListView<?> listView) {
        Label label = new Label(title);
        VBox panel = new VBox(6, label, listView);
        HBox.setHgrow(panel, Priority.ALWAYS);
        VBox.setVgrow(listView, Priority.ALWAYS);
        return panel;
    }

    /** Gives each typed list a readable display format. */
    private void configureListCells() {
        studentList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Student student, boolean empty) {
                super.updateItem(student, empty);
                setText(empty || student == null ? null : student.getId() + " - " + student.getName());
            }
        });
        assignmentList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Assignment assignment, boolean empty) {
                super.updateItem(assignment, empty);
                setText(empty || assignment == null ? null : assignment.id() + " - "
                        + assignment.title() + " (Max: " + assignment.maxPoints() + ")");
            }
        });
        groupList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Group group, boolean empty) {
                super.updateItem(group, empty);
                setText(empty || group == null ? null : group.getGroupId() + " - " + group.getGroupName());
            }
        });
    }

    /** Copies the current service data into the three visible lists. */
    private void refreshLists() {
        String selectedStudentId = selectedStudent == null ? null : selectedStudent.getId();
        String selectedAssignmentId = selectedAssignment == null ? null : selectedAssignment.id();
        studentList.setItems(FXCollections.observableArrayList(gradebookService.getStudents()));
        assignmentList.setItems(FXCollections.observableArrayList(gradebookService.getAssignments()));
        groupList.setItems(FXCollections.observableArrayList(gradebookService.getGroups()));
        if (selectedStudentId == null) {
            populateStudentEditor(null);
        } else {
            studentList.getItems().stream()
                    .filter(student -> student.getId().equals(selectedStudentId))
                    .findFirst()
                    .ifPresentOrElse(
                            student -> studentList.getSelectionModel().select(student),
                            () -> populateStudentEditor(null));
        }
        if (selectedAssignmentId == null) {
            updateAssignmentDetails(null);
        } else {
            assignmentList.getItems().stream()
                    .filter(assignment -> assignment.id().equals(selectedAssignmentId))
                    .findFirst()
                    .ifPresentOrElse(
                            assignment -> assignmentList.getSelectionModel().select(assignment),
                            () -> updateAssignmentDetails(null));
        }
    }

    /** Imports a gradebook selected by the user. */
    private void importGradebook(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import Gradebook");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON files", "*.json"));
        var file = chooser.showOpenDialog(stage);
        if (file == null) {
            return;
        }
        try {
            gradebookService = persistenceService.loadGradebook(file.toPath());
            refreshLists();
            statusLabel.setText("Imported: " + file.getName());
        } catch (IOException exception) {
            statusLabel.setText("Import failed: " + exception.getMessage());
        }
    }

    /** Saves the current gradebook to a user-selected JSON file. */
    private void saveGradebook(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Gradebook");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON files", "*.json"));
        var file = chooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }
        try {
            Path path = file.toPath();
            persistenceService.saveGradebook(path, gradebookService);
            statusLabel.setText("Saved: " + file.getName());
        } catch (IOException exception) {
            statusLabel.setText("Save failed: " + exception.getMessage());
        }
    }

    /** Creates predictable sample data for the initial screen. */
    private GradebookService createSampleGradebook() {
        GradebookService service = new GradebookService();
        service.addStudent(new Student("s1", "Alex"));
        service.addStudent(new Student("s2", "Sam"));
        service.addStudent(new Student("s3", "Jordan"));
        service.addStudent(new Student("s4", "Taylor"));
        service.addStudent(new Student("s5", "Morgan"));
        service.addAssignment(new Assignment("a1", "Team Project", 100.0, true));
        service.addAssignment(new Assignment("a2", "Midterm Exam", 80.0, false));
        service.addAssignment(new Assignment("a3", "Final Presentation", 50.0, true));
        service.addGroup(new Group("g1", "Blue Group"));
        service.addStudentToGroup("g1", "s1");
        service.addStudentToGroup("g1", "s2");
        return service;
    }

    /** Starts the JavaFX application. */
    public static void main(String[] args) {
        launch(args);
    }
}
