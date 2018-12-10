package gms.dataacquisition.cssloader.osdgateway.service;

import gms.dataacquisition.cssloader.osdgateway.CssLoaderOsdGateway;
import gms.dataacquisition.cssloader.osdgateway.service.configuration.Configuration;
import gms.dataacquisition.cssloader.osdgateway.service.configuration.ConfigurationLoader;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.factory.ProcessingStationReferenceFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.jpa.StationReferenceEntityManagerFactories;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.jpa.StationReferenceRepositoryJpa;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.influx.WaveformRepositoryInfluxDb;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.StationSohRepositoryJpa;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.WaveformsEntityManagerFactories;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.persistence.EntityManagerFactory;

/**
 * Main entry point for the osd-gateway-service.  Starts a web server.
 */
public class Application {

  private static Map<String, String> entityManagerProperties;

  public static void main(final String[] args) {
    Runtime.getRuntime().addShutdownHook(new Thread(
        CssLoaderOsdGatewayService::stopService));

    Configuration config = ConfigurationLoader.load();
    entityManagerProperties = entityMgrProps(config);
    CssLoaderOsdGatewayService.startService(osdGateway(), config);
  }

  private static Map<String, String> entityMgrProps(Configuration config) {
    Map<String, String> entityMgrProps = new HashMap<>();
    config.persistenceUrl.ifPresent(
        s -> entityMgrProps.put("hibernate.connection.url", s));
    return entityMgrProps;
  }

  private static EntityManagerFactory createEntityManagerFactory(
      Function<Map<String, String>, EntityManagerFactory> emFunc) {
    return emFunc.apply(entityManagerProperties);
  }

  /**
   * Returns a CssLoaderOsdGatewayAccessLibrary object containing the default configuration.
   */
  private static CssLoaderOsdGateway osdGateway() {
    EntityManagerFactory wfEntityManager = createEntityManagerFactory(
        WaveformsEntityManagerFactories::create);
    EntityManagerFactory refEntityManager = createEntityManagerFactory(
        StationReferenceEntityManagerFactories::create);

    return new CssLoaderOsdGateway(
        new WaveformRepositoryInfluxDb(wfEntityManager),
        new StationSohRepositoryJpa(wfEntityManager),
        new ProcessingStationReferenceFactory(
            new StationReferenceRepositoryJpa(
                refEntityManager)),
        new StationReferenceRepositoryJpa(refEntityManager));
  }


}
