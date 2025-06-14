package com.github.jrohatsch.moqqa.mqtt;

import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.Random;

public class StressTestMqtt {
    private final MqttAsyncClient client = new MqttAsyncClient("tcp://localhost:1883", "StressTestMqtt");

    public StressTestMqtt() throws MqttException {
    }

    public static void main(String[] args) throws MqttException, InterruptedException {
        var stresstest = new StressTestMqtt();
        stresstest.client.connect();
        while (!stresstest.client.isConnected()) {
            Thread.sleep(1_000);
        }

        for (int i = 0; i < 500; ++i) {
            int finalI = i;
            new Thread(() -> {
                while (true) {
                    try {
                        int number = new Random().nextInt(0, 100);
                        String message = String.valueOf(number);
                        stresstest.client.publish("root/hii" + finalI, message.getBytes(), 0, false);
                    } catch (MqttException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }

    }
}
