package com.github.jrohatsch.moqqa.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class TimeUtils {
    private final String[] zoneIds;
    private ZoneId selectedZoneId;

    public TimeUtils() {
        selectedZoneId = ZoneId.systemDefault();
        zoneIds = ZoneId.getAvailableZoneIds().toArray(new String[0]);
    }

    public void select(String zoneId) {
        System.out.println("changed timezone to " + zoneId);
        selectedZoneId = ZoneId.of(zoneId);
    }

    private LocalDateTime fromInstant(Instant instant) {
        return LocalDateTime.ofInstant(instant, selectedZoneId);
    }

    private LocalDateTime fromString(String time) {
        return LocalDateTime.parse(time, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public String format(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public String format(Instant instant) {
        return format(fromInstant(instant));
    }

    public String[] getZoneIds() {
        var copy = zoneIds.clone();
        Arrays.sort(copy);
        return copy;
    }

    public String getZoneId() {
        return selectedZoneId.toString();
    }

    public int compare(String timeA, Instant timeB) {
        LocalDateTime localDateTimeA = fromString(timeA);
        LocalDateTime localDateTimeB = fromInstant(timeB);
        return localDateTimeA.compareTo(localDateTimeB);
    }
}
