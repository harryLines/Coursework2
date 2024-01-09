@RunWith(AndroidJUnit4.class)
public class ProviderTest {

    private ContentResolver mContentResolver;

    @Before
    public void setUp() {
        mContentResolver = InstrumentationRegistry.getTargetContext().getContentResolver();
    }

    @Test
    public void testQueryGoals() {
        Uri goalsUri = Uri.parse("content://com.example.trailblazer/goals");
        Cursor cursor = mContentResolver.query(goalsUri, null, null, null, null);

        assertNotNull("Cursor should not be null", cursor);
        // Further assertions to check the data
    }

    // Other test methods for insert, update, delete, etc.
}
