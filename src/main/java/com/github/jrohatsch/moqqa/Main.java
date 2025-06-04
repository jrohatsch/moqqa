package com.github.jrohatsch.moqqa;

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