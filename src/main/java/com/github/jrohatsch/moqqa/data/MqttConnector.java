package com.github.jrohatsch.moqqa.data;

import com.github.jrohatsch.moqqa.domain.Message;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.function.Consumer;

public interface MqttConnector {
    boolean connect(String url);

    void disconnect();

    IMqttAsyncClient getClient(String url) throws MqttException;

    void setMessageConsumer(Consumer<Message> consumer);

    void publish(String topic, String message, boolean retained);

    void auth(MqttUsernamePassword credentials);

    void auth(MqttServerCertificate credentials);
}
