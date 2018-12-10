package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.QcMaskRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.SignalDetectionRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.factory.ProcessingStationReferenceFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.configuration.Configuration;
import org.junit.Test;
import org.mockito.Mockito;

public class SignalDetectionRepositoryServiceTests {

  private static QcMaskRepository mockQcMaskRepository = Mockito.mock(QcMaskRepository.class);
  private static SignalDetectionRepository mockSignalDetectionRepository = Mockito
      .mock(SignalDetectionRepository.class);
  private static ProcessingStationReferenceFactory mockStationReferenceFactory = Mockito
      .mock(ProcessingStationReferenceFactory.class);

  @Test
  public void testStartServiceChecksNull() throws Exception {
    Configuration config = Configuration.builder().build();

    TestUtilities.checkStaticMethodValidatesNullArguments(SignalDetectionRepositoryService.class,
        "startService", config, mockQcMaskRepository, mockSignalDetectionRepository,
        mockStationReferenceFactory);
  }
}
