package com.github.jrohatsch.moqqa.data.impl;

import com.github.jrohatsch.moqqa.data.MqttConnector;
import com.github.jrohatsch.moqqa.domain.Message;
import org.eclipse.paho.client.mqttv3.*;

import java.util.function.Consumer;


public class MqttConnectorImpl implements MqttConnector, MqttCallback {
    private IMqttAsyncClient client;
    private Consumer<Message> messageConsumer;

    @Override
    public IMqttAsyncClient getClient(String url) throws MqttException {
        return new MqttAsyncClient("tcp://" + url, "Moqqa");
    }

    @Override
    public boolean connect(String url) {
        try {
            client = getClient(url);
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
        messageConsumer.accept(Message.of(s, mqttMessage.toString()));
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }

    @Override
    public void setMessageConsumer(Consumer<Message> consumer) {
        this.messageConsumer = consumer;
    }

    @Override
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
