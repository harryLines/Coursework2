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

/**
 * A utility class containing TypeConverters for Room database to convert complex data types to
 * and from their representations in the database.
 */
public class Converters {
    private static Gson gson = new Gson();

    /**
     * Converts a list of LatLng objects to a string representation.
     *
     * @param latLngs The list of LatLng objects to be converted.
     * @return A string representation of the LatLng objects.
     */
    @TypeConverter
    public static String fromLatLngList(List<LatLng> latLngs) {
        if (latLngs == null || latLngs.isEmpty()) {
            return null;
        }
        return latLngs.stream()
                .map(latLng -> latLng.latitude + "," + latLng.longitude)
                .collect(Collectors.joining(";"));
    }

    /**
     * Converts a string representation of LatLng objects to a list of LatLng objects.
     *
     * @param latLngString The string representation of LatLng objects.
     * @return A list of LatLng objects.
     */
    @TypeConverter
    public static List<LatLng> toLatLngList(String latLngString) {
        if (latLngString == null || latLngString.isEmpty()) {
            return null;
        }
        String[] pairs = latLngString.split(";");
        List<LatLng> latLngList = new ArrayList<>();
        for (String pair : pairs) {
            String[] latLng = pair.split(",");
            latLngList.add(new LatLng(Double.parseDouble(latLng[0]), Double.parseDouble(latLng[1])));
        }
        return latLngList;
    }

    /**
     * Converts a list of Double objects to a string representation.
     *
     * @param doubles The list of Double objects to be converted.
     * @return A string representation of the Double objects.
     */
    @TypeConverter
    public static String fromDoubleList(List<Double> doubles) {
        if (doubles == null || doubles.isEmpty()) {
            return null;
        }
        return doubles.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    /**
     * Converts a string representation of Double objects to a list of Double objects.
     *
     * @param doubleString The string representation of Double objects.
     * @return A list of Double objects.
     */
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

    /**
     * Converts a string representation to a LatLng object.
     *
     * @param value The string representation of a LatLng object.
     * @return A LatLng object.
     */
    @TypeConverter
    public static LatLng fromString(String value) {
        if (value == null) {
            return null;
        }
        String[] latLng = value.split(",");
        return new LatLng(Double.parseDouble(latLng[0]), Double.parseDouble(latLng[1]));
    }

    /**
     * Converts a LatLng object to a string representation.
     *
     * @param latLng The LatLng object to be converted.
     * @return A string representation of the LatLng object.
     */
    @TypeConverter
    public static String latLngToString(LatLng latLng) {
        return latLng == null ? null : latLng.latitude + "," + latLng.longitude;
    }

    /**
     * Converts a timestamp represented as a Long to a Date object.
     *
     * @param value The timestamp as a Long.
     * @return The Date object representing the timestamp.
     */
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    /**
     * Converts a Date object to a timestamp represented as a Long.
     *
     * @param date The Date object to be converted.
     * @return The timestamp as a Long.
     */
    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

}
