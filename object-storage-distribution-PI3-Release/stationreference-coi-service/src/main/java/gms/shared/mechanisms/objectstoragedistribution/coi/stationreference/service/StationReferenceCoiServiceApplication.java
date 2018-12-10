package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service;

import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.StationReferenceRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.jpa.StationReferenceEntityManagerFactories;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.jpa.StationReferenceRepositoryJpa;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service.configuration.Configuration;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service.configuration.ConfigurationLoader;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManagerFactory;

/**
 * Application entry point for the Signal Detection Repository Service. Starts a {@link
 * StationReferenceCoiService} with loaded {@link Configuration} and a specific {@link
 * StationReferenceRepositoryInterface} implementation.
 */
public class StationReferenceCoiServiceApplication {

  private static EntityManagerFactory entityManagerFactory;

  /**
   * Obtains {@link EntityManagerFactory} from the {@link StationReferenceEntityManagerFactories}
   *
   * @return EntityManagerFactory, not null
   */
  private static EntityManagerFactory getEntityManagerFactory() {
    if (entityManagerFactory == null) {
      Configuration config = configuration();
      Map<String, String> entityMgrProps = new HashMap<>();
      config.getPersistenceUrl().ifPresent(s -> entityMgrProps.put("hibernate.connection.url", s));
      entityManagerFactory = StationReferenceEntityManagerFactories.create(entityMgrProps);
    }
    return entityManagerFactory;
  }

  /**
   * Obtains {@link Configuration} for the {@link StationReferenceCoiService}
   *
   * @return Configuration, not null
   */
  private static Configuration configuration() {
    return ConfigurationLoader.load();
  }

  /**
   * Obtains the {@link StationReferenceRepositoryInterface} for the {@link StationReferenceCoiService}
   *
   * @return QcMaskRepository, not null
   */
  private static StationReferenceRepositoryInterface stationReferenceRepositoryInterface() {
    return new StationReferenceRepositoryJpa(getEntityManagerFactory());
  }

  /**
   * Shutdowns spark, and closes EntityManagerFactory;
   */
  private static void shutdown() {
    StationReferenceCoiService.stopService();
    getEntityManagerFactory().close();
  }

  public static void main(String[] args) {
    Runtime.getRuntime()
        .addShutdownHook(new Thread(StationReferenceCoiServiceApplication::shutdown));
    StationReferenceCoiService.startService(configuration(), stationReferenceRepositoryInterface());
  }
}
