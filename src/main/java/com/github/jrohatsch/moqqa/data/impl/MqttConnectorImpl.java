package com.github.jrohatsch.moqqa.data.impl;

import com.github.jrohatsch.moqqa.data.MqttConnector;
import com.github.jrohatsch.moqqa.data.MqttUsernamePassword;
import com.github.jrohatsch.moqqa.domain.Message;
import org.eclipse.paho.client.mqttv3.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Properties;
import java.util.function.Consumer;


public class MqttConnectorImpl implements MqttConnector, MqttCallback {
    private IMqttAsyncClient client;
    private MqttConnectOptions connectOptions = new MqttConnectOptions();
    private Consumer<Message> messageConsumer;

    @Override
    public IMqttAsyncClient getClient(String url) throws MqttException {
        return new MqttAsyncClient("tcp://" + url, "Moqqa");
    }

    public void setUserNameAndPassword(String username, String password) {
        connectOptions.setUserName(username);
        connectOptions.setPassword(password.toCharArray());
    }

    @Override
    public boolean connect(String url) {
        try {
            client = getClient(url);
        } catch (MqttException e) {
            System.err.println(e);
            return false;
        }

        try {
            client.connect(connectOptions);
        } catch (MqttException e) {
            System.err.println(e);
            return false;
        }

        Instant start = Instant.now();
        while (!client.isConnected()) {
            try {
                Thread.sleep(100);
                if (Duration.between(start, Instant.now()).compareTo(Duration.ofSeconds(30)) > 0) {
                    return false;
                }
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
    public void publish(String topic, String message, boolean retained) {
        var mqttMessage = new MqttMessage(message.getBytes());
        mqttMessage.setRetained(retained);
        try {
            client.publish(topic, mqttMessage);
        } catch (MqttException e) {
            System.err.println("failed to publish message");
        }
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

    @Override
    public void auth(MqttUsernamePassword credentials) {
        setUserNameAndPassword(credentials.username(), credentials.password());
    }
}
