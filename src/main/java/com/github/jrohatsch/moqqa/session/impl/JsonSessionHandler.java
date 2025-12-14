package com.github.jrohatsch.moqqa.session.impl;

import com.github.jrohatsch.moqqa.session.Session;
import com.github.jrohatsch.moqqa.session.SessionHandler;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JsonSessionHandler implements SessionHandler {
    List<Session> sessions = new ArrayList<>();

    @Override
    public void save(Session session) {
        var mapper = new ObjectMapper();
        sessions.add(session);
        mapper.writeValue(new File(System.getProperty("user.home").concat("/.moqqa/sessions.json")), sessions);
    }

    @Override
    public List<Session> load() {
        return List.of(new Session("Local", "localhost:1883", true), new Session("Mosquitto Test", "mosquitto.org:1883", true));
    }

    @Override
    public Session load(String sessionName) {
        return null;
    }

    @Override
    public void delete(String sessionName) {

    }
}
