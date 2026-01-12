package com.github.jrohatsch.moqqa;

import com.github.jrohatsch.moqqa.data.impl.DatahandlerImpl;
import com.github.jrohatsch.moqqa.data.impl.MqttConnectorImpl;
import com.github.jrohatsch.moqqa.ui.UserInterface;

class Main {

    public static void main(String[] args) {
        MqttConnectorImpl mqttConnector = new MqttConnectorImpl();
        DatahandlerImpl dataHandler = new DatahandlerImpl(mqttConnector);
        UserInterface userInterface = new UserInterface(dataHandler);
        userInterface.show();
    }
}