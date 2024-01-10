import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.annotation.Nullable;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class LiveDataTestUtil {

    public static <T> T getValue(final LiveData<T> liveData) throws InterruptedException {
        final Object[] data = new Object[1];
        CountDownLatch latch = new CountDownLatch(1);

        Observer<T> observer = new Observer<T>() {
            @Override
            public void onChanged(@Nullable T t) {
                data[0] = t;
                latch.countDown();
                liveData.removeObserver(this);
            }
        };

        liveData.observeForever(observer);

        if (!latch.await(2, TimeUnit.SECONDS)) {
            throw new InterruptedException("LiveData value was never set.");
        }

        //noinspection unchecked
        return (T) data[0];
    }
}
