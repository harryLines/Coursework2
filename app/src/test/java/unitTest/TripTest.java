package unitTest;

import com.example.trailblazer.Trip;
import com.google.android.gms.maps.model.LatLng;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TripTest {

    @Test
    public void testGetSetDate() {
        Date date = new Date();
        Trip trip = new Trip(date, 10.0, Trip.MOVEMENT_WALK, 3600, new ArrayList<>(), new ArrayList<>(), 200, Trip.WEATHER_SUNNY, "image");

        assertEquals(date, trip.getDate());

        Date newDate = new Date();
        trip.setDate(newDate);

        assertEquals(newDate, trip.getDate());
    }

    @Test
    public void testGetSetMovementType() {
        Date date = new Date();
        Trip trip = new Trip(date, 10.0, Trip.MOVEMENT_WALK, 3600, new ArrayList<>(), new ArrayList<>(), 200, Trip.WEATHER_SUNNY, "image");

        assertEquals(Trip.MOVEMENT_WALK, trip.getMovementType());

        trip.setMovementType(Trip.MOVEMENT_RUN);

        assertEquals(Trip.MOVEMENT_RUN, trip.getMovementType());
    }

    @Test
    public void testGetSetDistance() {
        Date date = new Date();
        Trip trip = new Trip(date, 10.0, Trip.MOVEMENT_WALK, 3600, new ArrayList<>(), new ArrayList<>(), 200, Trip.WEATHER_SUNNY, "image");

        assertEquals(10.0, trip.getDistance(), 0.001);

        trip.setDistance(15.0);

        assertEquals(15.0, trip.getDistance(), 0.001);
    }

    @Test
    public void testGetSetTimeInSeconds() {
        Date date = new Date();
        Trip trip = new Trip(date, 10.0, Trip.MOVEMENT_WALK, 3600, new ArrayList<>(), new ArrayList<>(), 200, Trip.WEATHER_SUNNY, "image");

        assertEquals(3600, trip.getTimeInSeconds());

        trip.setTimeInSeconds(7200);

        assertEquals(7200, trip.getTimeInSeconds());
    }

    @Test
    public void testGetSetRoutePoints() {
        Date date = new Date();
        List<LatLng> route = new ArrayList<>();
        route.add(new LatLng(0, 0));
        route.add(new LatLng(1, 1));

        Trip trip = new Trip(date, 10.0, Trip.MOVEMENT_WALK, 3600, route, new ArrayList<>(), 200, Trip.WEATHER_SUNNY, "image");

        assertEquals(route, trip.getRoutePoints());

        List<LatLng> newRoute = new ArrayList<>();
        newRoute.add(new LatLng(2, 2));
        newRoute.add(new LatLng(3, 3));

        trip.setRoutePoints(newRoute);

        assertEquals(newRoute, trip.getRoutePoints());
    }

    @Test
    public void testGetSetElevationData() {
        Date date = new Date();
        List<Double> elevationData = new ArrayList<>();
        elevationData.add(100.0);
        elevationData.add(150.0);

        Trip trip = new Trip(date, 10.0, Trip.MOVEMENT_WALK, 3600, new ArrayList<>(), elevationData, 200, Trip.WEATHER_SUNNY, "image");

        assertEquals(elevationData, trip.getElevationData());

        List<Double> newElevationData = new ArrayList<>();
        newElevationData.add(200.0);
        newElevationData.add(250.0);

        trip.setElevationData(newElevationData);

        assertEquals(newElevationData, trip.getElevationData());
    }
}
