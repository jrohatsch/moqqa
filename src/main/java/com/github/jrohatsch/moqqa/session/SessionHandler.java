package com.github.jrohatsch.moqqa.session;

import java.util.List;
import java.util.Optional;

public interface SessionHandler {
    void save(Session session);

    List<Session> load();

    Optional<Session> load(String sessionName);

    void delete(String sessionName);
}
