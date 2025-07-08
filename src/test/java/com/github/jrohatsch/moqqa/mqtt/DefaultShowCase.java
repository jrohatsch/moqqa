package com.github.jrohatsch.moqqa.mqtt;

import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONObject;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Random;
import java.util.stream.Stream;

public class DefaultShowCase {
    private final MqttAsyncClient client = new MqttAsyncClient("tcp://localhost:1883", "StressTestMqtt");

    public DefaultShowCase() throws MqttException {
    }

    public static void main(String[] args) throws MqttException, InterruptedException {
        var stresstest = new DefaultShowCase();
        stresstest.client.connect();
        while (!stresstest.client.isConnected()) {
            Thread.sleep(1_000);
        }

        Stream.of("A","B","C","D").parallel().forEach(letter -> {
            while (true) {
                try {
                    stresstest.client.publish("rooms/room" + letter + "/name", ("Room " + letter).getBytes(), 0, false);
                    String temp = getRandomTemp();
                    stresstest.client.publish("rooms/room" + letter + "/temp", temp.getBytes(), 0, false);

                    stresstest.client.publish("data/client"+letter, getClientJson(letter).getBytes(), 0 ,false);

                    Thread.sleep(1000);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }

    private static String getClientJson(String letter) {
        JSONObject object = new JSONObject();
        object.put("id", "client"+letter);
        object.put("lastLogin", Instant.now().minus(2, ChronoUnit.HOURS));
        return object.toString();
    }

    private static String getRandomTemp() {
        Random random = new Random();
        int temp = random.nextInt(20,30);
        return "%d °C".formatted(temp);
    }
}
