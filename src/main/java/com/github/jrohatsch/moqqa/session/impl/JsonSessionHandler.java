package com.github.jrohatsch.moqqa.session.impl;

import com.github.jrohatsch.moqqa.session.Session;
import com.github.jrohatsch.moqqa.session.SessionHandler;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JsonSessionHandler implements SessionHandler {
    private Path folderPath = Path.of(System.getProperty("user.home"), "moqqa");
    private Path settingsFilePath = Path.of(System.getProperty("user.home"), "moqqa", "sessions.json");
    private ObjectMapper mapper = new ObjectMapper();
    List<Session> sessions = new ArrayList<>();

    @Override
    public void save(Session session) {
        sessions.removeIf(eachSession -> eachSession.name().equals(session.name()));
        sessions.add(session);

        writeFile();
    }

    private void writeFile() {
        if (!Files.exists(folderPath)) {
            try {
                Files.createDirectory(folderPath);
            } catch (IOException e) {
                System.err.printf("could not create direcotry %s%n!",folderPath);
            }
        }
        mapper.writeValue(settingsFilePath.toFile(), sessions);
    }

    @Override
    public List<Session> load() {
        if (Files.exists(settingsFilePath)) {
            List<Map<String,String>> read = mapper.readValue(settingsFilePath.toFile(), List.class);

            for (Map<String,String> map : read) {
                var session = new Session(map.get("name"), map.get("address"));
                sessions.add(session);
            }
        }
         return sessions;
    }

    @Override
    public Optional<Session> load(String sessionName) {
        if (Files.exists(settingsFilePath)) {
            List<Map<String,String>> read = mapper.readValue(settingsFilePath.toFile(), List.class);

            for (Map<String,String> map : read) {
                if (map.get("name").equals(sessionName)) {
                     return Optional.of(new Session(map.get("name"), map.get("address")));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void delete(String sessionName) {
        sessions.removeIf(session -> session.name().equals(sessionName));
        writeFile();
    }
}
