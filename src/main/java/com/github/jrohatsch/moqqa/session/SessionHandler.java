package com.github.jrohatsch.moqqa.session;

import java.util.List;

public interface SessionHandler {
    void save(Session session);

    List<Session> load();

    Session load(String sessionName);

    void delete(String sessionName);
}
