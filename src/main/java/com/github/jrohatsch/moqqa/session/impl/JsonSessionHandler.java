package com.github.jrohatsch.moqqa.session.impl;

import com.github.jrohatsch.moqqa.session.AuthenticationType;
import com.github.jrohatsch.moqqa.session.Session;
import com.github.jrohatsch.moqqa.session.SessionHandler;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JsonSessionHandler implements SessionHandler {
    private final Path folderPath = Path.of(System.getProperty("user.home"), "moqqa");
    private final Path settingsFilePath = Path.of(System.getProperty("user.home"), "moqqa", "sessions.json");
    private final ObjectMapper mapper = new ObjectMapper();
    final List<Session> sessions = new ArrayList<>();

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

    public List<Session> load() {
        sessions.clear();

        if (Files.exists(settingsFilePath)) {
            List<Map<String,Object>> read = mapper.readValue(settingsFilePath.toFile(), List.class);

            for (Map<String,Object> map : read) {
                String name = (String) map.get("name");
                String address = (String) map.get("address");

                if (map.containsKey("authentication")) {
                    Map<String, String> auth = (Map<String, String>) map.get("authentication");

                    if (auth.containsKey("type")) {
                        String type = auth.get("type");
                        if (AuthenticationType.USERNAME_PASSWORD.name().equals(type)) {
                            String username = auth.get("username");
                            String password = auth.get("password");
                            sessions.add(Session.usernamePassword(name, address, username, password));
                        } else if (AuthenticationType.SERVER_CERT.name().equals(type)) {
                            String certPath = auth.get("certPath");
                            sessions.add(Session.certPath(name, address, certPath));
                        } else if (AuthenticationType.ANONYMOUS.name().equals(type)) {
                            sessions.add(Session.anonymous(name, address));
                        }
                    } else {
                        sessions.add(Session.anonymous(name, address));
                    }
                }

            }
        }
         return new ArrayList<>(sessions);
    }

    @Override
    public Optional<Session> get(String sessionName) {
        return sessions.stream().filter(session -> session.name().equals(sessionName)).findFirst();
    }

    @Override
    public void delete(String sessionName) {
        sessions.removeIf(session -> session.name().equals(sessionName));
        writeFile();
    }
}
