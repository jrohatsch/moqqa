package com.github.jrohatsch.moqqa.components;

import com.github.jrohatsch.moqqa.data.Datahandler;
import com.github.jrohatsch.moqqa.data.MqttConnector;
import com.github.jrohatsch.moqqa.data.PathObserver;
import com.github.jrohatsch.moqqa.data.SelectionObserver;
import com.github.jrohatsch.moqqa.domain.Message;
import com.github.jrohatsch.moqqa.domain.PathListItem;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class PathItemInfoTest {

    Message mockMessage(String fullTopic) {
        var message = Mockito.mock(Message.class);
        Mockito.when(message.topic()).thenReturn(fullTopic);
        return message;
    }

    @Test
    void getJSONObject() {
        var datahandler = Mockito.mock(Datahandler.class);
        String topic = "parent";
        List<Message> messages = new ArrayList<>();

        messages.add(mockMessage("parent/child-A/name"));
        messages.add(mockMessage("parent/child-A/age"));

        messages.add(mockMessage("parent/child-B/name"));
        messages.add(mockMessage("parent/child-B/age"));

        Mockito.when(datahandler.getMessages(Mockito.any())).thenReturn(messages);

        var pathItemInfo = new PathItemInfo(datahandler);

        var jsonObject = pathItemInfo.getJSONObject(topic);

        assertEquals("{\"parent\":{\"child-B\":{\"name\":{},\"age\":{}},\"child-A\":{\"name\":{},\"age\":{}}}}", jsonObject.toString());
    }
}