package unitTest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;

import com.example.trailblazer.ui.HomeFragment;
import com.example.trailblazer.data.Trip;
import com.example.trailblazer.data.TripDao;

public class HomeFragmentUnitTest {

    @Mock
    private TripDao mockTripDao;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCalculateAverageSpeed() {
        HomeFragment homeFragment = new HomeFragment();

        List<Trip> trips = new ArrayList<>();
        Date date = new Date();

        // Distance in kilometers
        double distance1 = 10.0;
        double distance2 = 20.0;

        // Time in seconds
        long time1 = 3600;
        long time2 = 7200;

        // Calculate speed in km/h
        double speed1 = (distance1) / (time1);
        double speed2 = (distance2) / (time2);

        List<Double> elevationData = new ArrayList<>();
        elevationData.add(100.0);
        elevationData.add(150.0);

        Trip trip1 = new Trip(date, distance1, Trip.MOVEMENT_WALK, time1, new ArrayList<>(), elevationData, 200, Trip.WEATHER_SUNNY, "image");
        Trip trip2 = new Trip(date, distance2, Trip.MOVEMENT_WALK, time2, new ArrayList<>(), elevationData, 400, Trip.WEATHER_SUNNY, "image");

        trips.add(trip1);
        trips.add(trip2);

        double avgSpeed = homeFragment.calculateAverageSpeed(trips, Trip.MOVEMENT_WALK);

        double expectedAvgSpeed = (speed1 + speed2) / 2.0; // Calculate the expected average speed

        assertEquals(expectedAvgSpeed, avgSpeed, 0);
    }

    @Test
    public void testCalculateDistanceByDay() throws ParseException {
        HomeFragment homeFragment = new HomeFragment();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date1 = sdf.parse("2022-01-01");
        Date date2 = sdf.parse("2022-01-02");

        List<Trip> walkingTrips = new ArrayList<>();
        List<Double> elevationData = new ArrayList<>();
        elevationData.add(100.0);
        elevationData.add(150.0);

        Trip trip1 = new Trip(date1, 15.0, Trip.MOVEMENT_WALK, 3600, new ArrayList<>(), elevationData, 200, Trip.WEATHER_SUNNY, "image");
        Trip trip2 = new Trip(date2, 8.0, Trip.MOVEMENT_WALK, 7200, new ArrayList<>(), elevationData, 400, Trip.WEATHER_SUNNY, "image");

        walkingTrips.add(trip1);
        walkingTrips.add(trip2);

        Map<String, Double> distanceByDay = homeFragment.calculateDistanceByDay(walkingTrips);

        Map<String, Double> expectedDistanceByDay = new TreeMap<>();
        expectedDistanceByDay.put("2022-01-01", 15.0);
        expectedDistanceByDay.put("2022-01-02", 8.0);

        assertEquals(expectedDistanceByDay, distanceByDay);
    }

    @Test
    public void testFilterTripsLastWeek() throws ParseException {
        HomeFragment homeFragment = new HomeFragment();
        List<Double> elevationData = new ArrayList<>();
        elevationData.add(100.0);
        elevationData.add(150.0);

        // Adjust date1 and date2 to be within the last week
        Date date1 = new Date(System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000)); // Two days ago
        Date date2 = new Date(System.currentTimeMillis() - (5 * 24 * 60 * 60 * 1000)); // Five days ago

        // Add a date that is not within the last week
        Date date3 = new Date(System.currentTimeMillis() - (10 * 24 * 60 * 60 * 1000)); // Ten days ago

        List<Trip> trips = new ArrayList<>();
        Trip trip1 = new Trip(date1, 15.0, Trip.MOVEMENT_WALK, 3600, new ArrayList<>(), elevationData, 200, Trip.WEATHER_SUNNY, "image");
        Trip trip2 = new Trip(date2, 8.0, Trip.MOVEMENT_WALK, 7200, new ArrayList<>(), elevationData, 400, Trip.WEATHER_SUNNY, "image");
        Trip trip3 = new Trip(date3, 12.0, Trip.MOVEMENT_WALK, 5400, new ArrayList<>(), elevationData, 300, Trip.WEATHER_SUNNY, "image");

        trips.add(trip1);
        trips.add(trip2);
        trips.add(trip3);

        List<Trip> filteredTrips = homeFragment.filterTripsLastWeek(trips);

        assertEquals(2, filteredTrips.size()); // Only trip1 and trip2 should be within the last week
    }


}
