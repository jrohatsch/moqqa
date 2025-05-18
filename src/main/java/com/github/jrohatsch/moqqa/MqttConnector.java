package com.github.jrohatsch.moqqa;

import org.eclipse.paho.client.mqttv3.*;

import java.time.Instant;
import java.util.function.Consumer;


public class MqttConnector implements MqttCallback {
    private MqttAsyncClient client;
    private Consumer<Message> messageConsumer;

    public boolean connect(String url) {
        try {
            client = new MqttAsyncClient("tcp://" + url, "Moqqa");
        } catch (MqttException e) {
            return false;
        }

        try {
            client.connect();
        } catch (MqttException e) {
            return false;
        }
        while (!client.isConnected()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        try {
            client.subscribe("#", 0);
            client.setCallback(this);
        } catch (MqttException e) {
            return false;
        }

        return true;
    }

    @Override
    public void connectionLost(Throwable throwable) {

    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) {
        System.out.printf("got message on %s\n".formatted(s));
        messageConsumer.accept(new Message(s, mqttMessage.toString(), Instant.now()));
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }

    public void setMessageConsumer(Consumer<Message> consumer) {
        this.messageConsumer = consumer;
    }

    public void disconnect() {
        try {
            client.disconnect();

            while (client.isConnected()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } catch (MqttException e) {
            System.err.println(e);
        }
    }
}
