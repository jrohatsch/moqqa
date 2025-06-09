package com.github.jrohatsch.moqqa;

import com.github.jrohatsch.moqqa.data.impl.DatahandlerMqtt;
import com.github.jrohatsch.moqqa.data.impl.DatahandlerTesting;
import com.github.jrohatsch.moqqa.data.impl.MqttConnector;
import com.github.jrohatsch.moqqa.ui.UserInterface;

class Main {

    public static void main(String[] args) {
        MqttConnector mqttConnector = new MqttConnector();
        DatahandlerMqtt dataHandler = new DatahandlerMqtt(mqttConnector);
        UserInterface userInterface = new UserInterface(dataHandler);

        userInterface.setup();
        userInterface.show();
        userInterface.start();
    }
}