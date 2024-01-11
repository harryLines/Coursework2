package unitTest;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;

import com.example.trailblazer.ui.HomeFragmentViewModel;

public class HomeFragmentViewModelTest {

    @Rule
    public TestRule rule = new InstantTaskExecutorRule();

    @Mock
    private Observer<Boolean> walkingObserver;
    @Mock
    private Observer<Boolean> runningObserver;
    @Mock
    private Observer<Boolean> cyclingObserver;
    @Mock
    private Observer<Double> avgWalkSpeedObserver;
    @Mock
    private Observer<Double> avgRunSpeedObserver;
    @Mock
    private Observer<Double> avgCycleSpeedObserver;
    @Mock
    private Observer<Boolean> distanceObserver;
    @Mock
    private Observer<Boolean> caloriesObserver;

    private HomeFragmentViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        viewModel = new HomeFragmentViewModel();
    }

    @Test
    public void testInitialState() {
        assertNotNull(viewModel.isWalkingChecked().getValue());
        assertNotNull(viewModel.isRunningChecked().getValue());
        assertNotNull(viewModel.isCyclingChecked().getValue());
        assertNotNull(viewModel.getAvgWalkSpeed().getValue());
        assertNotNull(viewModel.getAvgRunSpeed().getValue());
        assertNotNull(viewModel.getAvgCycleSpeed().getValue());
        assertNotNull(viewModel.isDistanceChecked().getValue());
        assertNotNull(viewModel.isCaloriesChecked().getValue());

        assertEquals(false, viewModel.isWalkingChecked().getValue());
        assertEquals(false, viewModel.isRunningChecked().getValue());
        assertEquals(false, viewModel.isCyclingChecked().getValue());
        assertEquals(0.0, viewModel.getAvgWalkSpeed().getValue(), 0.0);
        assertEquals(0.0, viewModel.getAvgRunSpeed().getValue(), 0.0);
        assertEquals(0.0, viewModel.getAvgCycleSpeed().getValue(), 0.0);
        assertEquals(false, viewModel.isDistanceChecked().getValue());
        assertEquals(false, viewModel.isCaloriesChecked().getValue());
    }

    @Test
    public void testSetWalkingChecked() {
        viewModel.isWalkingChecked().observeForever(walkingObserver);

        viewModel.setWalkingChecked(true);

        verify(walkingObserver).onChanged(true);
    }

    @Test
    public void testSetRunningChecked() {
        viewModel.isRunningChecked().observeForever(runningObserver);

        viewModel.setRunningChecked(true);

        verify(runningObserver).onChanged(true);
    }

    @Test
    public void testSetCyclingChecked() {
        viewModel.isCyclingChecked().observeForever(cyclingObserver);

        viewModel.setCyclingChecked(true);

        verify(cyclingObserver).onChanged(true);
    }

    @Test
    public void testSetAvgWalkSpeed() {
        viewModel.getAvgWalkSpeed().observeForever(avgWalkSpeedObserver);

        viewModel.setAvgWalkSpeed(5.0);

        verify(avgWalkSpeedObserver).onChanged(5.0);
    }

    @Test
    public void testSetAvgRunSpeed() {
        viewModel.getAvgRunSpeed().observeForever(avgRunSpeedObserver);

        viewModel.setAvgRunSpeed(8.0);

        verify(avgRunSpeedObserver).onChanged(8.0);
    }

    @Test
    public void testSetAvgCycleSpeed() {
        viewModel.getAvgCycleSpeed().observeForever(avgCycleSpeedObserver);

        viewModel.setAvgCycleSpeed(15.0);

        verify(avgCycleSpeedObserver).onChanged(15.0);
    }

    @Test
    public void testSetDistanceChecked() {
        viewModel.isDistanceChecked().observeForever(distanceObserver);

        viewModel.setDistanceChecked(true);

        verify(distanceObserver).onChanged(true);
    }

    @Test
    public void testSetCaloriesChecked() {
        viewModel.isCaloriesChecked().observeForever(caloriesObserver);

        viewModel.setCaloriesChecked(true);

        verify(caloriesObserver).onChanged(true);
    }
}
