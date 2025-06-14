package com.github.jrohatsch.moqqa.data.impl;

import com.github.jrohatsch.moqqa.data.MqttConnector;
import com.github.jrohatsch.moqqa.domain.Message;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;
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
    public IMqttAsyncClient getClient(String url) throws MqttException {
        return Mockito.mock(IMqttAsyncClient.class);
    }

    @Override
    public void setMessageConsumer(Consumer<Message> consumer) {
        messageConsumer = consumer;
    }

    public void mockMessage(String topic, String value) {
        messageConsumer.accept(Message.of(topic, value));
    }
}
