package com.github.jrohatsch.moqqa.data.impl;

import com.github.jrohatsch.moqqa.data.MqttConnector;
import com.github.jrohatsch.moqqa.data.MqttServerCertificate;
import com.github.jrohatsch.moqqa.data.MqttUsernamePassword;
import com.github.jrohatsch.moqqa.domain.Message;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.mockito.Mockito;

import java.util.function.Consumer;

class TestMqttConnector implements MqttConnector {
    private Consumer<Message> messageConsumer;

    @Override
    public boolean connect(String url) {
        return true;
    }

    @Override
    public void disconnect() {

    }

    @Override
    public IMqttAsyncClient getClient(String url) {
        return Mockito.mock(IMqttAsyncClient.class);
    }

    @Override
    public void setMessageConsumer(Consumer<Message> consumer) {
        messageConsumer = consumer;
    }

    @Override
    public void publish(String topic, String message, boolean retained) {
        mockMessage(topic, message);
    }

    public void mockMessage(String topic, String value) {
        messageConsumer.accept(Message.of(topic, value));
    }

    @Override
    public void auth(MqttUsernamePassword credentials) {
       
    }

    @Override
    public void auth(MqttServerCertificate credentials) {

    }

    @Override
    public String getAddress() {
        return "tcp://localhost:1883";
    }

    @Override
    public boolean isConnected() {
        return true;
    }
}
