package unitTest;

import static com.example.trailblazer.ProgressCalculations.calculateTotalDistance;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

import com.example.trailblazer.Database;
import com.example.trailblazer.ProgressCalculations;
import com.example.trailblazer.ProgressFragment;
import com.example.trailblazer.Trip;

public class ProgressCalculationsUnitTest {

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCalculateTotalDistance() {
        List<Trip> trips = new ArrayList<>();
        List<Double> elevationData = new ArrayList<>();
        elevationData.add(100.0);
        elevationData.add(150.0);
        Date date = new Date();
        trips.add(new Trip(date, 1.0, Trip.MOVEMENT_WALK, 3600, new ArrayList<>(), elevationData, 200, Trip.WEATHER_SUNNY, "image")); // 1 km
        trips.add(new Trip(date, 1.5, Trip.MOVEMENT_WALK, 3600, new ArrayList<>(), elevationData, 200, Trip.WEATHER_SUNNY, "image"));  // 2 km
        trips.add(new Trip(date, 2.0, Trip.MOVEMENT_WALK, 3600, new ArrayList<>(), elevationData, 200, Trip.WEATHER_SUNNY, "image")); // 1.5 km

        double totalDistance = calculateTotalDistance(trips, Trip.MOVEMENT_WALK);
        assertEquals(4.5, totalDistance, 0.01); // Total walking distance is 2.5 km
    }

    @Test
    public void testCalculateAverageSpeed() {
        List<Trip> trips = new ArrayList<>();
        List<Double> elevationData = new ArrayList<>();
        elevationData.add(100.0);
        elevationData.add(150.0);
        Date date = new Date();
        trips.add(new Trip(date, 1000, Trip.MOVEMENT_WALK, 3600, new ArrayList<>(), elevationData, 200, Trip.WEATHER_SUNNY, "image")); // 1 km
        trips.add(new Trip(date, 2000, Trip.MOVEMENT_WALK, 3600, new ArrayList<>(), elevationData, 200, Trip.WEATHER_SUNNY, "image"));  // 2 km
        trips.add(new Trip(date, 1500, Trip.MOVEMENT_WALK, 3600, new ArrayList<>(), elevationData, 200, Trip.WEATHER_SUNNY, "image")); // 1.5 km

        double averageSpeed = ProgressCalculations.calculateAverageSpeed(trips, Trip.MOVEMENT_WALK);
        assertEquals(1.5, averageSpeed, 0);
    }

    @Test
    public void testCalculateTotalTime() {
        List<Trip> trips = new ArrayList<>();
        List<Double> elevationData = new ArrayList<>();
        elevationData.add(100.0);
        elevationData.add(150.0);
        Date date = new Date();
        trips.add(new Trip(date, 1.0, Trip.MOVEMENT_WALK, 3600, new ArrayList<>(), elevationData, 200, Trip.WEATHER_SUNNY, "image")); // 1 km
        trips.add(new Trip(date, 2.0, Trip.MOVEMENT_WALK, 7200, new ArrayList<>(), elevationData, 200, Trip.WEATHER_SUNNY, "image"));  // 2 km
        trips.add(new Trip(date, 1.5, Trip.MOVEMENT_WALK, 5400, new ArrayList<>(), elevationData, 200, Trip.WEATHER_SUNNY, "image")); // 1.5 km

        long totalTime = ProgressCalculations.calculateTotalTime(trips, Trip.MOVEMENT_WALK);
        assertEquals(16200, totalTime); // Total walking time is 11160 seconds
    }

    @Test
    public void testCalculatePercentageChange() {
        int percentChange1 = ProgressCalculations.calculatePercentageChange(10.0, 5.0);
        assertEquals(100, percentChange1);

        int percentChange2 = ProgressCalculations.calculatePercentageChange(5.0, 10.0);
        assertEquals(-50, percentChange2);

        int percentChange3 = ProgressCalculations.calculatePercentageChange(0.0, 0.0);
        assertEquals(0, percentChange3);

        int percentChange4 = ProgressCalculations.calculatePercentageChange(0.0, 5.0);
        assertEquals(-100, percentChange4);

        int percentChange5 = ProgressCalculations.calculatePercentageChange(5.0, 0.0);
        assertEquals(0, percentChange5);
    }

    @Test
    public void testFormatDistance() {
        String formattedDistance = ProgressCalculations.formatDistance(2500.0);
        assertEquals("2.50 km", formattedDistance);

        formattedDistance = ProgressCalculations.formatDistance(1000.0);
        assertEquals("1.00 km", formattedDistance);
    }

    @Test
    public void testFormatSpeed() {
        String formattedSpeed = ProgressCalculations.formatSpeed(5.5);
        assertEquals("5.5 km/h", formattedSpeed);

        formattedSpeed = ProgressCalculations.formatSpeed(10.0);
        assertEquals("10.0 km/h", formattedSpeed);
    }

    @Test
    public void testFormatTime() {
        String formattedTime = ProgressCalculations.formatTime(3661);
        assertEquals("1h 01m", formattedTime);

        formattedTime = ProgressCalculations.formatTime(7200);
        assertEquals("2h 00m", formattedTime);

        formattedTime = ProgressCalculations.formatTime(150);
        assertEquals("2m 30s", formattedTime);

        formattedTime = ProgressCalculations.formatTime(60);
        assertEquals("1m 00s", formattedTime);

        formattedTime = ProgressCalculations.formatTime(30);
        assertEquals("30s", formattedTime);
    }

    @Test
    public void testFormatTimeChange() {
        String formattedChange1 = ProgressCalculations.formatTimeChange(10);
        assertEquals("+10%", formattedChange1);

        String formattedChange2 = ProgressCalculations.formatTimeChange(-10);
        assertEquals("-10%", formattedChange2);

        String formattedChange3 = ProgressCalculations.formatTimeChange(0);
        assertEquals("+0%", formattedChange3);
    }
}
