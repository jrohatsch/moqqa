package com.github.jrohatsch.moqqa.domain;

import org.json.JSONException;
import org.json.JSONObject;

public class FormattedJson {
    private final String message;
    private final int lines;

    private FormattedJson(String message, int lines){
        this.message = message;
        this.lines = lines;
    }

    public static boolean isValid(Message message) {
        try {
            new JSONObject(message.message());
            return true;
        } catch (JSONException ignored) {
            return false;
        }
    }

    public static FormattedJson of(Message message) {
        try {
            var jsonObject = new JSONObject(message.message());
            String buffer = jsonObject.toString(2);
            int lines = buffer.split("\n").length;
            buffer = buffer.replaceAll("\n","<br>");
            buffer = "<html>"+buffer+"</html>";
            return new FormattedJson(buffer, lines);
        } catch (JSONException ignored) {
            return null;
        }
    }

    public int lines() {
        return lines;
    }

    @Override
    public String toString(){
        return message;
    }
}
