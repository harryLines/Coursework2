import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.trailblazer.MainActivity;
import com.example.trailblazer.R;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testLocationPermissionHandling() {
        // Use Espresso to interact with permission dialogs and validate the behavior
    }
    @Test
    public void testIntentHandlingCaseFragmentToShow() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        intent.putExtra("fragmentToShow", "Logging");
        ActivityScenario.launch(intent);

        onView(withId(R.id.tabLayout)).check(matches(isDisplayed()));
    }

    @Test
    public void testIntentHandlingCasestopLogging() throws InterruptedException {
        Intent intent = new Intent();
        intent.putExtra("fragmentToShow", "Logging");
        Thread.sleep(5000);
        ActivityScenario.launch(intent);
        // Validate that the activity handles the intent correctly
        // For example, checking if the correct tab is selected
    }
    @Test
    public void testIntentHandlingCaseNoIntent() {
        Intent intent = new Intent();
        intent.putExtra("fragmentToShow", "Logging");
        ActivityScenario.launch(intent);
        // Validate that the activity handles the intent correctly
        // For example, checking if the correct tab is selected
    }

    @Test
    public void testTabSelection() {
        // Use Espresso to perform click actions on tabs
        // Assert the displayed fragment or view pager's current item
    }
    @Test
    public void testMenuSelection() {
        // Open the menu and perform clicks on menu items
        // Assert the expected outcome, like launching a new activity
    }
    @Test
    public void testThemeChangeHandling() {
        // Simulate a theme change and verify if the activity reacts appropriately
    }
}