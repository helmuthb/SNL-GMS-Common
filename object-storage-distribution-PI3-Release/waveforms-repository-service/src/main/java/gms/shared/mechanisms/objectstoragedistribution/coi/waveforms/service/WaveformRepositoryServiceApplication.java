package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.service;

import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.RawStationDataFrameRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.StationSohRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.WaveformRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.influx.WaveformRepositoryInfluxDb;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.RawStationDataFrameRepositoryJpa;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.StationSohRepositoryJpa;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.WaveformsEntityManagerFactories;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.service.configuration.Configuration;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.service.configuration.ConfigurationLoader;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManagerFactory;

/**
 * Application entry point for the Waveforms Repository Service. Starts a {@link
 * WaveformRepositoryService} with loaded {@link Configuration} and a specific {@link
 * WaveformRepositoryInterface}, {@link StationSohRepositoryInterface},
 * and {@link RawStationDataFrameRepositoryInterface} implementation.
 */
public class WaveformRepositoryServiceApplication {

  private static EntityManagerFactory entityManagerFactory;

  /**
   * Obtains {@link EntityManagerFactory} from the {@link WaveformsEntityManagerFactories}
   *
   * @return EntityManagerFactory, not null
   */
  private static EntityManagerFactory getEntityManagerFactory() {
    if (entityManagerFactory == null) {
      Configuration config = configuration();
      Map<String, String> entityMgrProps = new HashMap<>();
      config.getPersistenceUrl().ifPresent(s -> entityMgrProps.put("hibernate.connection.url", s));
      entityManagerFactory = WaveformsEntityManagerFactories.create(entityMgrProps);
    }
    return entityManagerFactory;
  }

  /**
   * Obtains {@link Configuration} for the {@link WaveformRepositoryService}
   *
   * @return Configuration, not null
   */
  private static Configuration configuration() {
    return ConfigurationLoader.load();
  }

  /**
   * Obtains the {@link WaveformRepositoryInterface} for the {@link WaveformRepositoryService}
   *
   * @return waveform repository, not null
   */
  private static WaveformRepositoryInterface waveformRepositoryInterface() {
    return new WaveformRepositoryInfluxDb(getEntityManagerFactory());
  }

  /**
   * Obtains the {@link StationSohRepositoryInterface} for the {@link WaveformRepositoryService}
   *
   * @return station SOH repository, not null
   */
  private static StationSohRepositoryInterface stationSohRepositoryInterface() {
    return new StationSohRepositoryJpa(getEntityManagerFactory());
  }

  /**
   * Obtains the {@link RawStationDataFrameRepositoryInterface} for the {@link WaveformRepositoryService}
   *
   * @return frame repository, not null
   */
  private static RawStationDataFrameRepositoryInterface rawStationDataFrameRepositoryInterface() {
    return new RawStationDataFrameRepositoryJpa(getEntityManagerFactory());
  }

  /**
   * Shutdowns spark, and closes EntityManagerFactory;
   */
  private static void shutdown() {
    WaveformRepositoryService.stopService();
    getEntityManagerFactory().close();
  }

  public static void main(String[] args) {
    Runtime.getRuntime()
        .addShutdownHook(new Thread(WaveformRepositoryServiceApplication::shutdown));
    WaveformRepositoryService.startService(configuration(), waveformRepositoryInterface(),
        stationSohRepositoryInterface(), rawStationDataFrameRepositoryInterface());
  }
}
