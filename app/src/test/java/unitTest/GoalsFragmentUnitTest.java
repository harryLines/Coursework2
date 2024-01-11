package unitTest;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.anything;
import static org.mockito.Mockito.verify;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.example.trailblazer.data.Goal;
import com.example.trailblazer.ui.GoalsFragment;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
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
