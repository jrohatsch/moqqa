package com.github.jrohatsch.moqqa;

import com.github.jrohatsch.moqqa.data.DataHandler;
import com.github.jrohatsch.moqqa.data.MqttConnector;
import com.github.jrohatsch.moqqa.ui.UserInterface;

class Main {

    public static void main(String[] args) {
        MqttConnector mqttConnector = new MqttConnector();
        DataHandler dataHandler = new DataHandler(mqttConnector);
        UserInterface userInterface = new UserInterface(dataHandler);

        userInterface.setup();
        userInterface.show();
        userInterface.start();
    }
}