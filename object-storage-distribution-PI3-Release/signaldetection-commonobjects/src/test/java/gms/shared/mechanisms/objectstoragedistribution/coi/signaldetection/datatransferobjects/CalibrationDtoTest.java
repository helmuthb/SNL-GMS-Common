package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Calibration;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.TestFixtures;
import org.junit.Before;
import org.junit.Test;


/**
 * Created by jwvicke on 10/12/17.
 */
public class CalibrationDtoTest {

    private final Calibration calibration = TestFixtures.processingCalibration1;
    private String calibrationJson;

    @Before
    public void setup() throws Exception {
        assertNotNull(calibration);
        calibrationJson = TestFixtures.objMapper.writeValueAsString(calibration);
        assertNotNull(calibrationJson);
        assertTrue(calibrationJson.length() > 0);
    }

    /**
     * Tests that a ChannelDto created from a ProcessingChannel can be serialized and deserialized,
     * and still remain equal to both the original ChannelDto and the
     * ProcessingChannel from which it was created.
     * @throws Exception
     */
    @Test
    public void deserializeTest() throws Exception {
        Calibration deserializedCalibration = callDeserializer(calibrationJson);
        assertNotNull(deserializedCalibration);
        assertEquals(deserializedCalibration, calibration);
    }

    private static Calibration callDeserializer(String json) throws Exception {
        return TestFixtures.objMapper.readValue(json, Calibration.class);
    }
}
