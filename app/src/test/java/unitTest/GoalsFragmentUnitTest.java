package unitTest;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.anything;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.os.StrictMode;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;
import androidx.test.core.app.ActivityScenario;

import com.example.trailblazer.Database;
import com.example.trailblazer.Goal;
import com.example.trailblazer.GoalDao;
import com.example.trailblazer.GoalsFragment;
import com.example.trailblazer.GoalsFragmentViewModel;
import com.example.trailblazer.MainActivity;
import com.example.trailblazer.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GoalsFragmentUnitTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();
    GoalsFragment goalsFragment;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        goalsFragment = new GoalsFragment();
    }

    @Test
    public void testFilterCompletedGoals() {
        GoalsFragment goalsFragment = new GoalsFragment();
        List<Goal> goals = new ArrayList<>();
        goals.add(new Goal(Goal.METRIC_CALORIES, 2, Goal.TIMEFRAME_WEEK, 50, 100, new Date()));  // Completed goal
        goals.add(new Goal(Goal.METRIC_CALORIES, 2, Goal.TIMEFRAME_WEEK, 100, 100, new Date())); // Incomplete goal

        List<Goal> filteredGoals = goalsFragment.filterCompletedGoals(goals);

        // Ensure that only incomplete goals are in the filtered list
        for (Goal goal : filteredGoals) {
            assert(!goal.isComplete());
        }
    }
}
