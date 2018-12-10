package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame.AuthenticationStatus;
import java.time.Instant;
import java.util.UUID;
import org.junit.Test;

public class RawStationDataFrameTests {

  private final UUID id = UUID.randomUUID();
  private final UUID stationId = UUID.fromString("46947cc2-8c86-4fa1-a764-c9b9944614b7");
  private final String stationName = "staName";
  private final Instant payloadDataStartTime = Instant.EPOCH;
  private final Instant payloadDataEndTime = Instant.EPOCH.plusMillis(2000);
  private final Instant receptionTime = Instant.EPOCH.plusSeconds(10);
  private final byte[] rawPayload = new byte[50];
  private final AuthenticationStatus authenticationStatus = AuthenticationStatus.AUTHENTICATION_SUCCEEDED;
  private final CreationInfo creationInfo = CreationInfo.DEFAULT;

  @Test
  public void testEqualsAndHashcode() {
    TestUtilities.checkClassEqualsAndHashcode(RawStationDataFrame.class);
  }

  @Test
  public void testRawStationDataFrameCreateChecksNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        RawStationDataFrame.class, "create",
        stationId, AcquisitionProtocol.CD11, stationName, payloadDataStartTime,
        payloadDataEndTime, receptionTime,
        rawPayload, authenticationStatus, creationInfo);
  }

  @Test
  public void testRawStationDataFrameFromChecksNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        RawStationDataFrame.class, "from",
        id, stationId, AcquisitionProtocol.CD11, stationName, payloadDataStartTime,
        payloadDataEndTime, receptionTime,
        rawPayload, authenticationStatus, creationInfo);
  }

}
