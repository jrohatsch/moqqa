package com.github.jrohatsch.moqqa.session;

import java.util.Optional;

public interface AppConfigHandler {
    void saveConfig(AppConfig appConfig);
    AppConfig loadConfig();
}
