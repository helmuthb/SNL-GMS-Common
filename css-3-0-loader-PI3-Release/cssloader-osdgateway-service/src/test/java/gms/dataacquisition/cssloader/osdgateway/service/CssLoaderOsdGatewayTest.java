package gms.dataacquisition.cssloader.osdgateway.service;


import gms.dataacquisition.cssloader.osdgateway.CssLoaderOsdGateway;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.ProcessingStationReferenceFactoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.StationReferenceRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.StationSohRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.WaveformRepositoryInterface;
import org.junit.Test;
import org.mockito.Mockito;


/**
 * Tests the CssLoaderOsdGateway
 */
public class CssLoaderOsdGatewayTest {

  private static final StationSohRepositoryInterface sohPersistence =
      Mockito.mock(StationSohRepositoryInterface.class);
  private static final WaveformRepositoryInterface waveformPersistence =
      Mockito.mock(WaveformRepositoryInterface.class);
  private static final ProcessingStationReferenceFactoryInterface processingStationReference =
      Mockito.mock(ProcessingStationReferenceFactoryInterface.class);
  private static final StationReferenceRepositoryInterface referenceRepo =
      Mockito.mock(StationReferenceRepositoryInterface.class);

  @Test
  public void testOsdGatewayValidatesNullArgs() throws Exception {
    TestUtilities.checkConstructorValidatesNullArguments(CssLoaderOsdGateway.class,
        waveformPersistence, sohPersistence, processingStationReference, referenceRepo);
  }
}
