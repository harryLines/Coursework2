import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import androidx.room.Room;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.trailblazer.Database;
import com.example.trailblazer.Goal;
import com.example.trailblazer.GoalDao;
import com.example.trailblazer.Reminder;
import com.example.trailblazer.ReminderDao;
import com.example.trailblazer.SavedLocation;
import com.example.trailblazer.SavedLocationDao;
import com.example.trailblazer.Trip;
import com.example.trailblazer.TripDao;
import com.google.android.gms.maps.model.LatLng;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class DatabaseTest {
    private Database db;
    private GoalDao goalDao;
    private ReminderDao reminderDao;
    private SavedLocationDao savedLocationDao;
    private TripDao tripDao;

    @Before
    public void createDb() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        db = Room.inMemoryDatabaseBuilder(context, Database.class).build();
        goalDao = db.goalDao();
        reminderDao = db.reminderDao();
        savedLocationDao = db.savedLocationDao();
        tripDao = db.tripDao();
    }

    @After
    public void closeDb() {
        db.close();
    }
    // Goals

    @Test
    public void testUpdateGoals() {
        // Insert some test data
        Goal goal1 = new Goal(Goal.METRIC_KILOMETERS, 2, Goal.TIMEFRAME_WEEK, 0.0, 10.0, new Date());
        Goal goal2 = new Goal(Goal.METRIC_STEPS, 2, Goal.TIMEFRAME_DAY, 3.0, 15.0, new Date());
        long goalID1 = goalDao.addNewGoal(goal1);
        long goalID2 = goalDao.addNewGoal(goal2);
        goal1.setGoalID(goalID1);
        goal2.setGoalID(goalID2);
        // Update the goals
        List<Goal> updatedGoals = new ArrayList<>();
        Goal goal1Updated = new Goal(Goal.METRIC_STEPS, 6, Goal.TIMEFRAME_DAY, 5.0, 21.0, new Date());
        goal1Updated.setGoalID(goalID1);
        Goal goal2Updated = new Goal(Goal.METRIC_KILOMETERS, 8, Goal.TIMEFRAME_WEEK, 9.0, 18.0, new Date());
        goal2Updated.setGoalID(goalID2);
        updatedGoals.add(goal1Updated);
        updatedGoals.add(goal2Updated);
        goalDao.updateGoals(updatedGoals); // Use the updated method
        // Verify that the goals have been updated
        List<Goal> loadedGoals = goalDao.loadGoals();
        assertEquals(2, loadedGoals.size());
        assertEquals(21.0, loadedGoals.get(0).getTarget(), 0.001);
        assertEquals(18.0, loadedGoals.get(1).getTarget(), 0.001);
    }

    @Test
    public void testLoadGoals() {
        // Insert some test data
        Goal goal1 = new Goal(Goal.METRIC_KILOMETERS,2,Goal.TIMEFRAME_WEEK,0.0,10.0,new Date());
        Goal goal2 = new Goal(Goal.METRIC_STEPS,2,Goal.TIMEFRAME_DAY,3.0,15.0,new Date());
        goalDao.addNewGoal(goal1);
        goalDao.addNewGoal(goal2);

        // Load all goals
        List<Goal> goals = goalDao.loadGoals();

        // Verify that the loaded data matches the inserted data
        assertEquals(2, goals.size());
    }
    @Test
    public void testAddAndLoadGoals() {
        Goal goal = new Goal(Goal.METRIC_KILOMETERS,2,Goal.TIMEFRAME_WEEK,0.0,10.0,new Date());
        long id = goalDao.addNewGoal(goal);
        List<Goal> loadedGoals = goalDao.loadGoals();
        assertEquals(loadedGoals.get(0).getGoalID(), id);
    }
    // Reminders
    @Test
    public void testLoadRemindersForLocation() {
        // Insert some test data
        long locationId = 1;
        Reminder reminder1 = new Reminder(locationId, "Reminder1");
        Reminder reminder2 = new Reminder(locationId, "Reminder2");
        reminderDao.addNewReminder(reminder1);
        reminderDao.addNewReminder(reminder2);

        // Load reminders for a location
        List<Reminder> reminders = reminderDao.loadRemindersForLocation(locationId);

        // Verify that the loaded data matches the inserted data
        assertEquals(2, reminders.size());
    }
    @Test
    public void testLoadRemindersForLocationName() {
        // Insert some test data
        String locationName = "Home";
        SavedLocation location1 = new SavedLocation(locationName,new LatLng(37.3861,122.0839));
        long locationID = savedLocationDao.saveLocation(location1);

        Reminder reminder1 = new Reminder(locationID, "Reminder1");
        Reminder reminder2 = new Reminder(locationID, "Reminder2");
        reminderDao.addNewReminder(reminder1);
        reminderDao.addNewReminder(reminder2);

        // Load reminders for a location name
        List<Reminder> reminders = reminderDao.loadRemindersForLocationName(locationName);

        // Verify that the loaded data matches the inserted data
        assertEquals(2, reminders.size());
    }

    @Test
    public void testLoadReminders() {
        // Insert some test data
        Reminder reminder1 = new Reminder(1, "Reminder1");
        Reminder reminder2 = new Reminder(2, "Reminder2");
        reminderDao.addNewReminder(reminder1);
        reminderDao.addNewReminder(reminder2);

        // Load all reminders
        List<Reminder> reminders = reminderDao.loadReminders();

        // Verify that the loaded data matches the inserted data
        assertEquals(2, reminders.size());
    }

    @Test
    public void testRemoveReminder() {
        // Insert a test reminder
        Reminder reminder = new Reminder(1, "Reminder1");
        long reminderId = reminderDao.addNewReminder(reminder);

        // Remove the reminder
        reminderDao.removeReminder(reminderId);

        // Verify that the reminder has been removed
        List<Reminder> reminders = reminderDao.loadReminders();
        assertEquals(0, reminders.size());
    }

    @Test
    public void testDeleteReminders() {
        // Insert some test data
        Reminder reminder1 = new Reminder(1, "Reminder1");
        Reminder reminder2 = new Reminder(2, "Reminder2");
        reminderDao.addNewReminder(reminder1);
        reminderDao.addNewReminder(reminder2);

        // Delete all reminders
        reminderDao.deleteReminders();

        // Verify that there are no reminders left
        List<Reminder> reminders = reminderDao.loadReminders();
        assertEquals(0, reminders.size());
    }

    @Test
    public void testReminderExists() {
        // Insert a test reminder
        long locationId = 1;
        String reminderText = "Reminder1";
        Reminder reminder = new Reminder(locationId, reminderText);
        reminderDao.addNewReminder(reminder);

        // Check if the reminder exists
        boolean exists = reminderDao.reminderExists(locationId, reminderText);
        assertTrue(exists);
    }

    @Test
    public void testAddAndLoadReminders() {
        Reminder reminder = new Reminder(1,"Reminder1");
        long reminderID = reminderDao.addNewReminder(reminder);
        List<Reminder> reminders = reminderDao.loadReminders();
        assertEquals(reminders.get(0).getId(), reminderID);
    }
    // Saved Locations
    @Test
    public void testDeleteSavedLocations() {
        SavedLocation location1 = new SavedLocation("Home",new LatLng(37.3861,122.0839));
        SavedLocation location2 = new SavedLocation("Work",new LatLng(35.3861,123.0839));
        savedLocationDao.saveLocation(location1);
        savedLocationDao.saveLocation(location2);

        savedLocationDao.deleteSavedLocations();

        // Verify that there are no saved locations
        List<SavedLocation> savedLocations = savedLocationDao.loadSavedLocations();
        assertEquals(0, savedLocations.size());
    }
    @Test
    public void testLoadSavedLocations() {
        SavedLocation location1 = new SavedLocation("Home",new LatLng(37.3861,122.0839));
        SavedLocation location2 = new SavedLocation("Work",new LatLng(35.3861,123.0839));
        savedLocationDao.saveLocation(location1);
        savedLocationDao.saveLocation(location2);

        List<SavedLocation> savedLocations = savedLocationDao.loadSavedLocations();

        assertEquals(2, savedLocations.size());
        assertEquals(location1.getName(), savedLocations.get(0).getName());
        assertEquals(location2.getName(), savedLocations.get(1).getName());
    }
    @Test
    public void testSaveAndLoadLocations() {
        SavedLocation location = new SavedLocation("Home",new LatLng(37.3861,122.0839));
        long locationID = savedLocationDao.saveLocation(location);
        List<SavedLocation> locations = savedLocationDao.loadSavedLocations();
        assertEquals(locations.get(0).getLocationID(), locationID);
    }
    // Trips
    @Test
    public void testInsertAndLoadTrips() {
        List<LatLng> routePoints = new ArrayList<>();
        routePoints.add(new LatLng(40.7128, -74.0060));
        routePoints.add(new LatLng(34.0522, -118.2437));
        List<Double> elevationData = new ArrayList<>();
        elevationData.add(250.0);
        elevationData.add(300.0);
        Trip trip = new Trip(new Date(), 1.0, Trip.MOVEMENT_RUN, 120, routePoints, elevationData, 25,Trip.WEATHER_FOGGY,"");
        long tripID = tripDao.addNewTrip(trip);
        List<Trip> trips = tripDao.loadTripHistory();
        assertEquals(trips.get(0).getTripID(), tripID);
    }
    @Test
    public void testDeleteTripHistory() {
        List<LatLng> routePoints = new ArrayList<>();
        routePoints.add(new LatLng(40.7128, -74.0060));
        routePoints.add(new LatLng(34.0522, -118.2437));
        List<Double> elevationData = new ArrayList<>();
        elevationData.add(250.0);
        elevationData.add(300.0);
        Trip trip = new Trip(new Date(), 1.0, Trip.MOVEMENT_RUN, 120, routePoints, elevationData, 25,Trip.WEATHER_FOGGY,"");
        tripDao.addNewTrip(trip);

        tripDao.deleteTripHistory();

        // Load trips and check if the list is empty
        List<Trip> trips = tripDao.loadTripHistory();
        assertTrue(trips.isEmpty());
    }
    @Test
    public void testLoadTripHistory() {
        List<LatLng> routePoints = new ArrayList<>();
        routePoints.add(new LatLng(40.7128, -74.0060));
        routePoints.add(new LatLng(34.0522, -118.2437));
        List<Double> elevationData = new ArrayList<>();
        elevationData.add(250.0);
        elevationData.add(300.0);
        Trip trip = new Trip(new Date(), 1.0, Trip.MOVEMENT_RUN, 120, routePoints, elevationData, 25,Trip.WEATHER_FOGGY,"");
        tripDao.addNewTrip(trip);

        List<Trip> trips = tripDao.loadTripHistory();
        assertFalse(trips.isEmpty());
    }

}
