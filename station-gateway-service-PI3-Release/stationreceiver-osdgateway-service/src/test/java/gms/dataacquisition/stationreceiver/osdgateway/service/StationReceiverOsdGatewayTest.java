package gms.dataacquisition.stationreceiver.osdgateway.service;


import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.ProcessingStationReferenceFactoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.RawStationDataFrameRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.StationSohRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.WaveformRepositoryInterface;
import org.junit.Test;
import org.mockito.Mockito;


public class StationReceiverOsdGatewayTest {

  @Test
  public void testNullParameterValidation() throws Exception {
    TestUtilities.checkConstructorValidatesNullArguments(
        StationReceiverOsdGateway.class,
        Mockito.mock(StationSohRepositoryInterface.class),
        Mockito.mock(WaveformRepositoryInterface.class),
        Mockito.mock(RawStationDataFrameRepositoryInterface.class),
        Mockito.mock(ProcessingStationReferenceFactoryInterface.class));
  }

}
