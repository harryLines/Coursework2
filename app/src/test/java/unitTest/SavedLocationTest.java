package unitTest;

import com.example.trailblazer.R;
import com.example.trailblazer.Reminder;
import com.example.trailblazer.SavedLocation;
import com.google.android.gms.maps.model.LatLng;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SavedLocationTest {

    @Test
    public void testGetSetName() {
        SavedLocation savedLocation = new SavedLocation("Park", new LatLng(0, 0), new ArrayList<>());

        assertEquals("Park", savedLocation.getName());

        savedLocation.setName("Home");

        assertEquals("Home", savedLocation.getName());
    }

    @Test
    public void testGetSetLatLng() {
        LatLng initialLatLng = new LatLng(0, 0);
        SavedLocation savedLocation = new SavedLocation("Park", initialLatLng, new ArrayList<>());

        assertEquals(initialLatLng, savedLocation.getLatLng());

        LatLng newLatLng = new LatLng(1, 1);
        savedLocation.setLatLng(newLatLng);

        assertEquals(newLatLng, savedLocation.getLatLng());
    }

    @Test
    public void testGetSetReminders() {
        SavedLocation savedLocation = new SavedLocation("Office", new LatLng(0, 0));
        Reminder reminder1 = new Reminder(savedLocation.getLocationID(),"Reminder1");
        Reminder reminder2 = new Reminder(savedLocation.getLocationID(),"Reminder1");
        List<Reminder> initialReminders = Arrays.asList(reminder1, reminder2);
        savedLocation.setReminders(initialReminders);
        assertEquals(initialReminders, savedLocation.getReminders());
    }

    @Test
    public void testAddReminder() {
        SavedLocation savedLocation = new SavedLocation("Home", new LatLng(0, 0), new ArrayList<>());

        assertEquals(0, savedLocation.getReminders().size());

        savedLocation.addReminder(new Reminder(savedLocation.getLocationID(),"Feed the cat"));

        assertEquals(1, savedLocation.getReminders().size());
        assertEquals("Feed the cat", savedLocation.getReminders().get(0).getReminderText());
    }

    @Test
    public void testGetRemindersAsString() {
        SavedLocation savedLocation = new SavedLocation("Office", new LatLng(0, 0));
        List<Reminder> reminders = Arrays.asList(new Reminder(savedLocation.getLocationID(),"Meeting"), new Reminder(savedLocation.getLocationID(),"Eating"));
        savedLocation.setReminders(reminders);
        assertEquals("Meeting,Eating,", savedLocation.getRemindersAsString());
    }

    @Test
    public void testGetSetEntered() {
        SavedLocation savedLocation = new SavedLocation("Park", new LatLng(0, 0), new ArrayList<>());

        assertFalse(savedLocation.isEntered());

        savedLocation.setEntered(true);

        assertTrue(savedLocation.isEntered());
    }

    @Test
    public void testGetSetLocationID() {
        SavedLocation savedLocation = new SavedLocation("Park", new LatLng(0, 0), new ArrayList<>());

        assertEquals(0, savedLocation.getLocationID());

        savedLocation.setLocationID(2);

        assertEquals(2, savedLocation.getLocationID());
    }
}
