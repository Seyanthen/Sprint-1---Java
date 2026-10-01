package com.gradebook;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
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
    private final Label statusLabel = new Label("Sample gradebook loaded.");
    private GradebookService gradebookService;
    private Student selectedStudent;

    /** Builds the toolbar and three-panel gradebook view. */
    @Override
    public void start(Stage stage) {
        gradebookService = createSampleGradebook();
        configureListCells();
        configureStudentEditor();
        refreshLists();
        studentList.getSelectionModel().selectedItemProperty()
                .addListener((observable, previous, current) -> populateStudentEditor(current));

        Button importButton = new Button("Import Gradebook");
        importButton.setOnAction(event -> importGradebook(stage));

        Button saveButton = new Button("Save Gradebook");
        saveButton.setOnAction(event -> saveGradebook(stage));

        ToolBar toolbar = new ToolBar(importButton, saveButton);
        HBox panels = new HBox(12,
                createPanel("Students", studentList),
                createPanel("Assignments", assignmentList),
                createPanel("Groups", groupList));
        VBox bottomPanel = new VBox(8, createStudentEditor(), statusLabel);

        BorderPane root = new BorderPane();
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
        TableColumn<Grade, String> scoreColumn = new TableColumn<>("Score");
        scoreColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(Double.toString(cell.getValue().score())));
        gradesTable.getColumns().setAll(List.of(assignmentColumn, scoreColumn));
        gradesTable.setPlaceholder(new Label("Select a student to view grades."));
        gradesTable.setPrefHeight(120);
    }

    /** Creates the editable bottom panel for the currently selected student. */
    private VBox createStudentEditor() {
        Button saveChangesButton = new Button("Save Changes");
        saveChangesButton.setOnAction(event -> saveStudentChanges());
        HBox fields = new HBox(8,
                new Label("Student ID:"), studentIdField,
                new Label("Name:"), studentNameField,
                saveChangesButton);
        return new VBox(6, new Label("Selected Student"), fields, gradesTable);
    }

    /** Populates or clears the editor when the student selection changes. */
    private void populateStudentEditor(Student student) {
        selectedStudent = student;
        if (student == null) {
            studentIdField.clear();
            studentNameField.clear();
            gradesTable.setItems(FXCollections.observableArrayList());
            return;
        }

        studentIdField.setText(student.getId());
        studentNameField.setText(student.getName());
        gradesTable.setItems(FXCollections.observableArrayList(student.getGrades().values()));
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
                setText(empty || assignment == null ? null : assignment.id() + " - " + assignment.title());
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
        service.addAssignment(new Assignment("a1", "Team Project", 100.0, true));
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
