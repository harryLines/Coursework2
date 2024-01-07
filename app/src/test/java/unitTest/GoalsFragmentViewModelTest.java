package unitTest;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.trailblazer.Goal;
import com.example.trailblazer.GoalsFragmentViewModel;

public class GoalsFragmentViewModelTest {
    @Rule
    public TestRule rule = new InstantTaskExecutorRule();

    @Mock
    private Observer<List<Goal>> observer;
    private GoalsFragmentViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        viewModel = new GoalsFragmentViewModel();
    }

    @Test
    public void testGetGoalsList() {
        MutableLiveData<List<Goal>> goalsListLiveData = viewModel.getGoalsList();
        assertNotNull(goalsListLiveData);
    }

    @Test
    public void testSetGoalsList() {
        List<Goal> testGoals = createTestGoals();
        viewModel.getGoalsList().observeForever(observer);

        viewModel.setGoalsList(testGoals);

        verify(observer).onChanged(testGoals);
    }

    @Test
    public void testSetNullGoalsList() {
        GoalsFragmentViewModel viewModel = new GoalsFragmentViewModel();
        viewModel.setGoalsList(null);

        // Directly call the method on the real instance and then verify
        List<Goal> goalsList = viewModel.getGoalsList().getValue();
        assertNull(goalsList);
    }

    @Test
    public void testSetAndGetGoalsList() {
        List<Goal> testGoals = createTestGoals();
        viewModel.setGoalsList(testGoals);

        assertEquals(testGoals, viewModel.getGoalsList().getValue());
    }

    // Helper method to create a list of test goals
    private List<Goal> createTestGoals() {
        List<Goal> testGoals = new ArrayList<>();
        testGoals.add(new Goal(1, Goal.METRIC_CALORIES, 2, Goal.TIMEFRAME_WEEK, 50, 100, new Date()));
        testGoals.add(new Goal(2, Goal.METRIC_KILOMETERS, 3, Goal.TIMEFRAME_MONTH, 20, 50, new Date()));
        return testGoals;
    }

    @After
    public void tearDown() {
        viewModel.getGoalsList().removeObserver(observer);
    }
}
