//package unitTest;
//
//import com.example.trailblazer.R;
//import com.example.trailblazer.Reminder;
//import com.example.trailblazer.SavedLocation;
//import com.google.android.gms.maps.model.LatLng;
//
//import org.junit.Test;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//import static org.junit.Assert.assertEquals;
//
//public class SavedLocationTest {
//
//    @Test
//    public void testGetSetName() {
//        SavedLocation savedLocation = new SavedLocation(1, "Park", new LatLng(0, 0), new ArrayList<>());
//
//        assertEquals("Park", savedLocation.getName());
//
//        savedLocation.setName("Home");
//
//        assertEquals("Home", savedLocation.getName());
//    }
//
//    @Test
//    public void testGetSetLatLng() {
//        LatLng initialLatLng = new LatLng(0, 0);
//        SavedLocation savedLocation = new SavedLocation(1, "Park", initialLatLng, new ArrayList<>());
//
//        assertEquals(initialLatLng, savedLocation.getLatLng());
//
//        LatLng newLatLng = new LatLng(1, 1);
//        savedLocation.setLatLng(newLatLng);
//
//        assertEquals(newLatLng, savedLocation.getLatLng());
//    }
//
//    @Test
//    public void testGetSetReminders() {
//        List<String> initialReminders = Arrays.asList("Meeting", "Gym");
//        SavedLocation savedLocation = new SavedLocation(1, "Office", new LatLng(0, 0), initialReminders);
//
//        assertEquals(initialReminders, savedLocation.getReminders());
//
//        List<Reminder> newReminders = Arrays.asList(n, "Call mom");
//        savedLocation.setReminders(newReminders);
//
//        assertEquals(newReminders, savedLocation.getReminders());
//    }
//
//    @Test
//    public void testAddReminder() {
//        SavedLocation savedLocation = new SavedLocation(1, "Home", new LatLng(0, 0), new ArrayList<>());
//
//        assertEquals(0, savedLocation.getReminders().size());
//
//        savedLocation.addReminder("Feed the cat");
//
//        assertEquals(1, savedLocation.getReminders().size());
//        assertEquals("Feed the cat", savedLocation.getReminders().get(0));
//    }
//
//    @Test
//    public void testGetRemindersAsString() {
//        List<String> reminders = Arrays.asList("Meeting", "Gym", "Buy groceries");
//        SavedLocation savedLocation = new SavedLocation(1, "Office", new LatLng(0, 0), reminders);
//
//        assertEquals("Meeting,Gym,Buy groceries,", savedLocation.getRemindersAsString());
//    }
//
//    @Test
//    public void testGetSetEntered() {
//        SavedLocation savedLocation = new SavedLocation(1, "Park", new LatLng(0, 0), new ArrayList<>());
//
//        assertEquals(false, savedLocation.isEntered());
//
//        savedLocation.setEntered(true);
//
//        assertEquals(true, savedLocation.isEntered());
//    }
//
//    @Test
//    public void testGetSetLocationID() {
//        SavedLocation savedLocation = new SavedLocation(1, "Park", new LatLng(0, 0), new ArrayList<>());
//
//        assertEquals(1, savedLocation.getLocationID());
//
//        savedLocation.setLocationID(2);
//
//        assertEquals(2, savedLocation.getLocationID());
//    }
//}
