package com.github.jrohatsch.moqqa.data.impl;

import com.github.jrohatsch.moqqa.components.SpecificCertificateTrust;
import com.github.jrohatsch.moqqa.data.MqttConnector;
import com.github.jrohatsch.moqqa.data.MqttServerCertificate;
import com.github.jrohatsch.moqqa.data.MqttUsernamePassword;
import com.github.jrohatsch.moqqa.domain.Message;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.function.Consumer;
import java.util.logging.Logger;


public class MqttConnectorImpl implements MqttConnector, MqttCallback {
    private final Logger LOGGER = Logger.getLogger(getClass().getSimpleName());
    private IMqttAsyncClient client;
    private MqttConnectOptions connectOptions = new MqttConnectOptions();
    private final MemoryPersistence memoryPersistence = new MemoryPersistence();
    private Consumer<Message> messageConsumer;
    private String protocol = "tcp";
    private String address = "";

    @Override
    public IMqttAsyncClient getClient(String url) throws MqttException {
        address = "%s://%s".formatted(protocol, url);
        connectOptions.setKeepAliveInterval(10);
        return new MqttAsyncClient(address, "Moqqa", memoryPersistence);
    }

    public void setUserNameAndPassword(String username, String password) {
        connectOptions.setUserName(username);
        connectOptions.setPassword(password.toCharArray());
    }

    public void setCAFile(String path) throws Exception {
        LOGGER.info("switching to ssl and using certificate " + path);
        protocol = "ssl";
        connectOptions.setSocketFactory(SpecificCertificateTrust.createCustomSSLFactory(path));
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
            client.connect(connectOptions).waitForCompletion();
        } catch (MqttException e) {
            System.err.println(e);
            return false;
        }

        if (!client.isConnected()) {
            return false;
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
        System.out.println("Connection Lost!");
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
            client.disconnect().waitForCompletion();
            LOGGER.info("disconnected");
            connectOptions = new MqttConnectOptions();
            address = "";
            protocol = "tcp";
        } catch (MqttException e) {
            System.err.println(e);
        }
    }

    @Override
    public void auth(MqttUsernamePassword credentials) {
        setUserNameAndPassword(credentials.username(), credentials.password());
    }

    @Override
    public void auth(MqttServerCertificate credentials) {
        try {
            setCAFile(credentials.path());
        } catch (Exception ignored) {

        }
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public boolean isConnected() {
        return client.isConnected();
    }
}
