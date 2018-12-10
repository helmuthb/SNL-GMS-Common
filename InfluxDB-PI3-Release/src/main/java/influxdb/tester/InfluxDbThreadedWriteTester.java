package influxdb.tester;


import java.util.ArrayList;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by jwvicke on 12/1/17.
 */
public class InfluxDbThreadedWriteTester {

    private static final int
            NUM_THREADS = 10,
            WRITES_PER_THREAD = 1000,
            SAMPLES_PER_WRITE = 500;

    private static final Random rand = new Random();

    private static Runnable worker = () ->
    {
        LongSummaryStatistics summary = new LongSummaryStatistics();
        int id = rand.nextInt();

        long t1 = System.currentTimeMillis();
        for (int i = 0; i < WRITES_PER_THREAD; i++) {
            long writeTime = InfluxDbWriteTester.doTest("THREAD_TEST" + rand.nextInt(),
                    SAMPLES_PER_WRITE);
            summary.accept(writeTime);
        }
        long t2 = System.currentTimeMillis();

        System.out.println(formatWithWorkerId(id, "total time = " + (t2-t1)));
        System.out.println(formatWithWorkerId(id, "avg time = " + summary.getAverage()));
        System.out.println(formatWithWorkerId(id, "min time = " + summary.getMin()));
        System.out.println(formatWithWorkerId(id, "max time = " + summary.getMax()));
        System.out.println(formatWithWorkerId(id, "total writes = " + summary.getCount()));
        System.out.println("**********************************");
    };

    public static void run() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        List<Future> futures = new ArrayList<>();
        for (int i = 0; i < NUM_THREADS; i++) {
            Future f = executor.submit(worker);
            futures.add(f);
        }
        // This loop is important;
        // without it, the threads may not complete before the executor shuts them down!
        for (Future f : futures) {
            f.get();
        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
    }

    private static String formatWithWorkerId(int id, String s) {
        return "worker " + id + "; " + s;
    }

}