package postgres.tester;

import data.Constants;
import data.WaveformGenerators;
import influxdb.tester.InfluxDbWriteTester;
import postgres.repository.WaveformBlobDao;
import postgres.repository.WaveformBlobRepository;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by jwvicke on 11/29/17.
 */
public class PostgresTester {

    private static final WaveformBlobRepository waveformRepository = new WaveformBlobRepository();
    private static final UUID
            ID_1 = UUID.fromString("1f1a3629-ffaf-4190-b59d-5ca6f0646fd6"),
            ID_2 = UUID.fromString("2f1a3629-ffaf-4190-b59d-5ca6f0646fd6"),
            ID_3 = UUID.fromString("3f1a3629-ffaf-4190-b59d-5ca6f0646fd6"),
            ID_4 = UUID.fromString("4f1a3629-ffaf-4190-b59d-5ca6f0646fd6"),
            ID_5 = UUID.fromString("5f1a3629-ffaf-4190-b59d-5ca6f0646fd6"),
            ID_6 = UUID.fromString("6f1a3629-ffaf-4190-b59d-5ca6f0646fd6"),
            ID_7 = UUID.fromString("7f1a3629-ffaf-4190-b59d-5ca6f0646fd6");

    private static final List<UUID> ids = Arrays.asList(ID_1, ID_2, ID_3, ID_4, ID_5, ID_6, ID_7);


    public static void runWriteTests() throws Exception {
        for (int i = 0; i < Constants.TEST_SIZES.length; i++) {
            Instant now = Instant.now();
            int size = Constants.TEST_SIZES[i];
            UUID id = ids.get(i);
            WaveformBlobDao wfb = new WaveformBlobDao(id, UUID.randomUUID(),
                    now, now.plusSeconds(30), WaveformGenerators.buildSamples(size));
            long time = runWriteTest(wfb);
            System.out.println(
                    String.format("PostgresTester: wrote waveform with %d points in %d millis",
                            size, time ));
        }
    }

    public static void runReadTests() throws Exception {
        for (UUID id : ids) {
            runReadTest(id);
        }
    }

    private static WaveformBlobDao runReadTest(UUID id) throws Exception {
        long t1 = System.currentTimeMillis();
        WaveformBlobDao wf = waveformRepository.getById(id);
        long t2 = System.currentTimeMillis();
        System.out.println(
                String.format("PostgresTester: Read waveform with %d points in %d millis",
                        wf.getSamples().length, (t2 - t1)));
        return wf;
    }

    private static long runWriteTest(WaveformBlobDao waveformBlob) throws Exception {
        long t1 = System.currentTimeMillis();
        waveformRepository.storeWaveformBlob(waveformBlob);
        long t2 = System.currentTimeMillis();
        return t2 - t1;
    }
}
