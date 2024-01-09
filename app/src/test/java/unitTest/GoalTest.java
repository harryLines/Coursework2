package unitTest;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;

import com.example.trailblazer.Goal;

public class GoalTest {

    @Test
    public void testGetFormattedDueDate_Expired() {
        Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);

        // Create a goal with a due date in the past
        Goal goal = createGoalWithDueDate(calendar, -1, Goal.TIMEFRAME_DAY);

        // Verify
        assertEquals("Expired", goal.getFormattedDueDate());
    }

    @Test
    public void testGetFormattedDueDate_DaysLeft() {
        Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);

        // Create a goal with a due date one day from now
        Goal goal = createGoalWithDueDate(calendar, 1, Goal.TIMEFRAME_DAY);

        // Verify
        assertEquals("1 days left", goal.getFormattedDueDate());
    }

    @Test
    public void testGetFormattedDueDate_OneWeekLeft() {
        Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);

        // Create a goal with a due date one week from now
        Goal goal = createGoalWithDueDate(calendar, 1, Goal.TIMEFRAME_WEEK);

        // Verify
        assertEquals("1 week left", goal.getFormattedDueDate());
    }

    @Test
    public void testGetFormattedDueDate_MonthsLeft() {
        Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);

        // Create a goal with a due date one month from now
        Goal goal = createGoalWithDueDate(calendar, 1, Goal.TIMEFRAME_MONTH);

        // Verify
        assertEquals("1 month left", goal.getFormattedDueDate());
    }

    @Test
    public void testGetDueDate_DayTimeframe() {
        Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);

        // Create a goal with a due date one day from now
        Goal goal = createGoalWithDueDate(calendar, 1, Goal.TIMEFRAME_DAY);

        // Get due date
        Date dueDate = goal.getDueDate();

        // Verify
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        assertEquals(calendar.getTime(), dueDate);
    }

    @Test
    public void testGetDueDate_WeekTimeframe() {
        Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);

        // Create a goal with a due date one week from now
        Goal goal = createGoalWithDueDate(calendar, 1, Goal.TIMEFRAME_WEEK);

        // Get due date
        Date dueDate = goal.getDueDate();

        // Verify
        calendar.add(Calendar.WEEK_OF_YEAR, 1);
        assertEquals(calendar.getTime(), dueDate);
    }

    @Test
    public void testGetDueDate_MonthTimeframe() {
        Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);

        // Create a goal with a due date one month from now
        Goal goal = createGoalWithDueDate(calendar, 1, Goal.TIMEFRAME_MONTH);

        // Get due date
        Date dueDate = goal.getDueDate();

        // Verify
        calendar.add(Calendar.MONTH, 1);
        assertEquals(calendar.getTime(), dueDate);
    }

    @Test
    public void testGetTargetWithUnitString_Calories() {
        Goal goal = new Goal(Goal.METRIC_CALORIES, 1, Goal.TIMEFRAME_DAY, 0, 500, new Date());

        assertEquals("500 kcal", goal.getTargetWithUnitString());
    }

    @Test
    public void testGetTargetWithUnitString_Kilometers() {
        Goal goal = new Goal(Goal.METRIC_KILOMETERS, 1, Goal.TIMEFRAME_DAY, 0, 10.5, new Date());

        assertEquals("10.50 km", goal.getTargetWithUnitString());
    }

    @Test
    public void testGetTargetWithUnitString_Steps() {
        Goal goal = new Goal(Goal.METRIC_STEPS, 1, Goal.TIMEFRAME_DAY, 0, 10000, new Date());

        assertEquals("10000 steps", goal.getTargetWithUnitString());
    }

    @Test
    public void testGetMetricTypeAsText_Calories() {
        Goal goal = new Goal(Goal.METRIC_CALORIES, 1, Goal.TIMEFRAME_DAY, 0, 500, new Date());

        assertEquals("Calories", goal.getMetricTypeAsText());
    }

    @Test
    public void testGetMetricTypeAsText_Kilometers() {
        Goal goal = new Goal(Goal.METRIC_KILOMETERS, 1, Goal.TIMEFRAME_DAY, 0, 10.5, new Date());

        assertEquals("Kilometers", goal.getMetricTypeAsText());
    }

    @Test
    public void testGetMetricTypeAsText_Steps() {
        Goal goal = new Goal(Goal.METRIC_STEPS, 1, Goal.TIMEFRAME_DAY, 0, 10000, new Date());

        assertEquals("Steps", goal.getMetricTypeAsText());
    }

    // Helper method to create a goal with a specific due date
    private Goal createGoalWithDueDate(Calendar calendar, int numberOfTimeframes, int timeframeType) {
        Date dateCreated = calendar.getTime();
        return new Goal(Goal.METRIC_CALORIES, numberOfTimeframes, timeframeType, 0, 0, dateCreated);
    }
}
