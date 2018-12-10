package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.service;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.RawStationDataFrameRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.StationSohRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.WaveformRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.influx.WaveformRepositoryInfluxDb;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.RawStationDataFrameRepositoryJpa;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.StationSohRepositoryJpa;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.service.configuration.Configuration;
import org.junit.Test;
import org.mockito.Mockito;


public class WaveformRepositoryServiceTests {

  private static WaveformRepositoryInterface mockWaveformRepository = Mockito
      .mock(WaveformRepositoryInfluxDb.class);

  private static StationSohRepositoryInterface mockStationSohRepository = Mockito
      .mock(StationSohRepositoryJpa.class);

  private static RawStationDataFrameRepositoryInterface mockFrameRepository = Mockito
      .mock(RawStationDataFrameRepositoryJpa.class);

  @Test
  public void testStartServiceChecksNull() throws Exception {
    Configuration config =
        Configuration.builder().build();

    TestUtilities.checkStaticMethodValidatesNullArguments(WaveformRepositoryService.class,
        "startService", config, mockWaveformRepository, mockStationSohRepository,
        mockFrameRepository);
  }
}
