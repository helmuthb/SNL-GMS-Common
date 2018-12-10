package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.StationReferenceRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service.configuration.Configuration;
import org.junit.Test;
import org.mockito.Mockito;


public class StationReferenceCoiServiceTests {

  private static StationReferenceRepositoryInterface mockRepo = Mockito
      .mock(StationReferenceRepositoryInterface.class);

  @Test
  public void testStartServiceChecksNull() throws Exception {
    Configuration config =
        Configuration.builder().build();

    TestUtilities.checkStaticMethodValidatesNullArguments(StationReferenceCoiService.class,
        "startService", config, mockRepo);
  }
}
