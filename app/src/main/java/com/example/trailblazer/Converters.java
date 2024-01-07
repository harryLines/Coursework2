package com.example.trailblazer;

import androidx.room.TypeConverter;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Converters {
    private static Gson gson = new Gson();
    @TypeConverter
    public static List<Reminder> stringToReminders(String data) {
        if (data == null) {
            return Collections.emptyList();
        }

        Type listType = new TypeToken<List<Reminder>>() {}.getType();
        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String remindersToString(List<Reminder> reminders) {
        return gson.toJson(reminders);
    }

    @TypeConverter
    public static String fromLatLngList(List<LatLng> latLngs) {
        if (latLngs == null || latLngs.isEmpty()) {
            return null;
        }
        return latLngs.stream()
                .map(latLng -> latLng.latitude + "," + latLng.longitude)
                .collect(Collectors.joining(";"));
    }

    @TypeConverter
    public static List<LatLng> toLatLngList(String latLngString) {
        if (latLngString == null || latLngString.isEmpty()) {
            return null;
        }
        List<String> pairs = Arrays.asList(latLngString.split(";"));
        List<LatLng> latLngList = new ArrayList<>();
        for (String pair : pairs) {
            String[] latLng = pair.split(",");
            latLngList.add(new LatLng(Double.parseDouble(latLng[0]), Double.parseDouble(latLng[1])));
        }
        return latLngList;
    }

    @TypeConverter
    public static String fromDoubleList(List<Double> doubles) {
        if (doubles == null || doubles.isEmpty()) {
            return null;
        }
        return doubles.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    @TypeConverter
    public static List<Double> toDoubleList(String doubleString) {
        if (doubleString == null || doubleString.isEmpty()) {
            return null;
        }
        String[] stringList = doubleString.split(",");
        List<Double> doubleList = new ArrayList<>();
        for (String s : stringList) {
            doubleList.add(Double.parseDouble(s));
        }
        return doubleList;
    }

    @TypeConverter
    public static LatLng fromString(String value) {
        if (value == null) {
            return null;
        }
        String[] latLng = value.split(",");
        return new LatLng(Double.parseDouble(latLng[0]), Double.parseDouble(latLng[1]));
    }

    @TypeConverter
    public static String latLngToString(LatLng latLng) {
        return latLng == null ? null : latLng.latitude + "," + latLng.longitude;
    }
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static List<String> fromStringToList(String value) {
        // Implement conversion from String to List<String>
        if (value == null) {
            return null;
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(value, type);
    }

    @TypeConverter
    public static String fromListToString(List<String> list) {
        // Implement conversion from List<String> to String
        if (list == null) {
            return null;
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<String>>() {}.getType();
        return gson.toJson(list, type);
    }
}
