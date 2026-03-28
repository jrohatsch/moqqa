package com.github.jrohatsch.moqqa.session;

import java.util.List;
import java.util.Optional;

public interface AppConfigHandler {
    void saveConfig(AppConfig appConfig);
    AppConfig loadConfig();
    void saveSession(Session session);
    List<Session> loadSession();
    Optional<Session> getSession(String sessionName);
    void deleteSession(String sessionName);
}
