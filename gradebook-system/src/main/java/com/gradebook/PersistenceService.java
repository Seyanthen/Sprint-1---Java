package com.gradebook;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/** Saves and restores a {@link GradebookService} as readable JSON. */
public class PersistenceService {
    private final ObjectMapper objectMapper;

    /** Creates a persistence service with pretty-printed JSON output enabled. */
    public PersistenceService() {
        objectMapper = new ObjectMapper().enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);
    }

    /** Writes the complete gradebook to the supplied file. */
    public void saveGradebook(Path path, GradebookService service) throws IOException {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(service, "service");

        try (var writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            objectMapper.writeValue(writer, service);
        }
    }

    /** Reads a gradebook from the supplied JSON file. */
    public GradebookService loadGradebook(Path path) throws IOException {
        Objects.requireNonNull(path, "path");

        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            GradebookSnapshot snapshot = objectMapper.readValue(reader, GradebookSnapshot.class);
            GradebookService service = new GradebookService();
            snapshot.students().forEach(student -> service.addStudent(
                    new Student(student.id(), student.name(), student.grades())));
            snapshot.assignments().forEach(service::addAssignment);
            snapshot.groups().forEach(group -> service.addGroup(
                    new Group(group.groupId(), group.groupName(), group.studentIds())));
            return service;
        }
    }

    /** Describes the JSON properties emitted by GradebookService's getters. */
    private record GradebookSnapshot(
            List<StudentSnapshot> students,
            List<Assignment> assignments,
            List<GroupSnapshot> groups) {
    }

    /** Serializable view of a student used when reading JSON. */
    private record StudentSnapshot(String id, String name, java.util.Map<String, Grade> grades) {
    }

    /** Serializable view of a group used when reading JSON. */
    private record GroupSnapshot(String groupId, String groupName, java.util.Set<String> studentIds) {
    }
}
