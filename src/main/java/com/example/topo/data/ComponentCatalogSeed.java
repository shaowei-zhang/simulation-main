package com.example.topo.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public final class ComponentCatalogSeed {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ComponentCatalogSeed() {
    }

    public record CatalogEntry(String kind, String name, String category, String description, Map<String, Object> catalogJson) {
    }

    public static List<CatalogEntry> defaultEntries() {
        Path libraryRoot = resolveLibraryRoot();
        if (Files.notExists(libraryRoot)) {
            return List.of();
        }

        try (Stream<Path> paths = Files.walk(libraryRoot)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing(path -> path.toString(), String::compareTo))
                    .map(ComponentCatalogSeed::readEntry)
                    .filter(Objects::nonNull)
                    .toList();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read element-library catalog from disk", e);
        }
    }

    private static Path resolveLibraryRoot() {
        Path projectRoot = Paths.get(System.getProperty("user.dir"));
        List<Path> candidates = List.of(
            projectRoot.resolve("src").resolve("element-library"),
            projectRoot.resolve("..").resolve("src").resolve("element-library"),
            projectRoot.resolve("..").resolve("..").resolve("src").resolve("element-library"),
            projectRoot.resolve("..").resolve("..").resolve("sldeditor-master").resolve("src").resolve("element-library")
        );
        return candidates.stream()
            .map(path -> path.toAbsolutePath().normalize())
            .filter(Files::isDirectory)
            .findFirst()
            .orElse(candidates.get(0).toAbsolutePath().normalize());
    }

    private static CatalogEntry readEntry(Path path) {
        try {
            Map<String, Object> root = OBJECT_MAPPER.readValue(path.toFile(), new TypeReference<>() {
            });
            String id = asString(root.get("id"));
            if (id == null || id.isBlank()) {
                return null;
            }

            String category = asString(root.get("category"));
            String name = asString(root.get("name"));
            String description = asString(root.get("description"));
            return new CatalogEntry(id, name == null ? id : name, category == null ? "custom" : category, description, root);
        } catch (IOException e) {
            return null;
        }
    }

    private static String asString(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }
}
