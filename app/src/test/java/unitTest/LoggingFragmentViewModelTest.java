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

import com.example.trailblazer.ui.LoggingFragmentViewModel;

public class LoggingFragmentViewModelTest {

    @Rule
    public TestRule rule = new InstantTaskExecutorRule();
    @Mock
    private Observer<Double> distanceObserver;
    @Mock
    private Observer<Integer> caloriesObserver;
    @Mock
    private Observer<Long> secondsObserver;
    @Mock
    private Observer<String> savedLocationNameObserver;
    @Mock
    private Observer<Integer> weatherObserver;
    @Mock
    private Observer<Integer> stepsObserver;
    @Mock
    private Observer<Boolean> walkingCheckedObserver;
    @Mock
    private Observer<Boolean> runningCheckedObserver;
    @Mock
    private Observer<Boolean> cyclingCheckedObserver;
    @Mock
    private Observer<Boolean> isTrackingObserver;

    private LoggingFragmentViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        viewModel = new LoggingFragmentViewModel();
    }

    @Test
    public void testInitialState() {
        assertNotNull(viewModel.getDistance().getValue());
        assertNotNull(viewModel.getCalories().getValue());
        assertNotNull(viewModel.getSeconds().getValue());
        assertNotNull(viewModel.getSavedLocationName().getValue());
        assertNotNull(viewModel.getWeather().getValue());
        assertNotNull(viewModel.getSteps().getValue());
        assertNotNull(viewModel.getWalkingChecked().getValue());
        assertNotNull(viewModel.getRunningChecked().getValue());
        assertNotNull(viewModel.getCyclingChecked().getValue());
        assertNotNull(viewModel.getIsTracking().getValue());

        assertEquals(0.0, viewModel.getDistance().getValue(), 0.0);
        assertEquals(0, (int) viewModel.getCalories().getValue());
        assertEquals(0L, (long) viewModel.getSeconds().getValue());
        assertEquals("", viewModel.getSavedLocationName().getValue());
        assertEquals(0, (int) viewModel.getWeather().getValue());
        assertEquals(0, (int) viewModel.getSteps().getValue());
        assertEquals(false, viewModel.getWalkingChecked().getValue());
        assertEquals(false, viewModel.getRunningChecked().getValue());
        assertEquals(false, viewModel.getCyclingChecked().getValue());
        assertEquals(false, viewModel.getIsTracking().getValue());
    }

    @Test
    public void testSetDistance() {
        viewModel.getDistance().observeForever(distanceObserver);

        viewModel.setDistance(10.5);

        verify(distanceObserver).onChanged(10.5);
    }

    @Test
    public void testSetCalories() {
        viewModel.getCalories().observeForever(caloriesObserver);

        viewModel.setCalories(200);

        verify(caloriesObserver).onChanged(200);
    }

    @Test
    public void testSetSeconds() {
        viewModel.getSeconds().observeForever(secondsObserver);

        viewModel.setSeconds(120L);

        verify(secondsObserver).onChanged(120L);
    }

    @Test
    public void testSetSavedLocationName() {
        viewModel.getSavedLocationName().observeForever(savedLocationNameObserver);

        viewModel.setSavedLocationName("Park");

        verify(savedLocationNameObserver).onChanged("Park");
    }

    @Test
    public void testSetWeather() {
        viewModel.getWeather().observeForever(weatherObserver);

        viewModel.setWeather(1);

        verify(weatherObserver).onChanged(1);
    }

    @Test
    public void testSetSteps() {
        viewModel.getSteps().observeForever(stepsObserver);

        viewModel.setSteps(5000);

        verify(stepsObserver).onChanged(5000);
    }

    @Test
    public void testSetWalkingChecked() {
        viewModel.getWalkingChecked().observeForever(walkingCheckedObserver);

        viewModel.setWalkingChecked(true);

        verify(walkingCheckedObserver).onChanged(true);
    }

    @Test
    public void testSetRunningChecked() {
        viewModel.getRunningChecked().observeForever(runningCheckedObserver);

        viewModel.setRunningChecked(true);

        verify(runningCheckedObserver).onChanged(true);
    }

    @Test
    public void testSetCyclingChecked() {
        viewModel.getCyclingChecked().observeForever(cyclingCheckedObserver);

        viewModel.setCyclingChecked(true);

        verify(cyclingCheckedObserver).onChanged(true);
    }

    @Test
    public void testSetIsTracking() {
        viewModel.getIsTracking().observeForever(isTrackingObserver);

        viewModel.setIsTracking(true);

        verify(isTrackingObserver).onChanged(true);
    }
}