package com.github.jrohatsch.moqqa.session.impl;

import com.github.jrohatsch.moqqa.session.*;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

public class JsonAppConfigHandler implements AppConfigHandler {
    private final Path folderPath = Path.of(System.getProperty("user.home"), "moqqa");
    private final Path settingsFilePath = Path.of(System.getProperty("user.home"), "moqqa", "sessions.json");
    private final Path configFilePath = Path.of(System.getProperty("user.home"), "moqqa", "config.json");
    private final ObjectMapper mapper = new ObjectMapper();
    final List<Session> sessions = new ArrayList<>();
    private final Logger LOGGER = Logger.getLogger(JsonAppConfigHandler.class.getName());

    @Override
    public void saveSession(Session session) {
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


    String readString(String key, Map<String, Object> map, String defaultValue) {
        try {
            if (map.containsKey(key) && map.get(key) != null && map.get(key) != "") {
                return (String) map.get(key);
            } else {
                LOGGER.warning("key %s is empty! using default value %s".formatted(key, defaultValue));
                return defaultValue;
            }
        } catch (ClassCastException e) {
            LOGGER.warning("could not cast value of key %s to String!%n".formatted(key));
            return defaultValue;
        }
    }

    @Override
    public void saveConfig(AppConfig appConfig) {
        if (!Files.exists(folderPath)) {
                try {
                    Files.createDirectory(folderPath);
                } catch (IOException e) {
                    System.err.printf("could not create direcotry %s%n!",folderPath);
                }
            }
            mapper.writeValue(configFilePath.toFile(), appConfig);
    }

    @Override
    public AppConfig loadConfig() {
        AppConfig fallBackConfig = new AppConfig( 1.0);
        if (Files.exists(configFilePath)) {
            try {
                return mapper.readValue(configFilePath.toFile(), AppConfig.class);
            } catch (JacksonException e) {
                LOGGER.warning("could not read config file");
            }
        } else {
            LOGGER.warning("config file does not exist");
        }
        return fallBackConfig;
    }

    public List<Session> loadSession() {
        sessions.clear();

        if (Files.exists(settingsFilePath)) {
            List<Map<String,Object>> read = mapper.readValue(settingsFilePath.toFile(), List.class);

            for (Map<String,Object> map : read) {
                String name = readString("name", map, "Unnamed Session");
                String address = readString("address", map, "localhost");
                Instant connection = Instant.parse(readString("connect", map, Instant.EPOCH.toString()));

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
                            sessions.add(Session.anonymous(name, address, connection));
                        }
                    } else {
                        sessions.add(Session.anonymous(name, address, connection));
                    }
                }

            }
        }

        sessions.sort(Comparator.comparing(Session::connect).reversed());

         return new ArrayList<>(sessions);
    }

    @Override
    public Optional<Session> getSession(String sessionName) {
        return sessions.stream().filter(session -> session.name().equals(sessionName)).findFirst();
    }

    @Override
    public void deleteSession(String sessionName) {
        sessions.removeIf(session -> session.name().equals(sessionName));
        writeFile();
    }
}
