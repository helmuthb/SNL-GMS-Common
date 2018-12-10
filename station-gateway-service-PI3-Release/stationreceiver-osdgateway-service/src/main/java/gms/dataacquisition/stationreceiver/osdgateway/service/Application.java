package gms.dataacquisition.stationreceiver.osdgateway.service;

import gms.dataacquisition.stationreceiver.osdgateway.service.configuration.Configuration;
import gms.dataacquisition.stationreceiver.osdgateway.service.configuration.ConfigurationLoader;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.factory.ProcessingStationReferenceFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.StationReferenceRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.jpa.StationReferenceEntityManagerFactories;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.jpa.StationReferenceRepositoryJpa;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.influx.WaveformRepositoryInfluxDb;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.RawStationDataFrameRepositoryJpa;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.StationSohRepositoryJpa;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.WaveformsEntityManagerFactories;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManagerFactory;


/**
 * Main entry point for the osd-gateway-service.  Starts a web server.
 */
public class Application {

  private static Map<String, String> entityManagerProperties;

  public static void main(final String[] args) {
    Runtime.getRuntime().addShutdownHook(new Thread(
        StationReceiverOsdGatewayService::stopService));

    Configuration config = ConfigurationLoader.load();
    entityManagerProperties = entityMgrProps(config);
    StationReceiverOsdGatewayService.startService(osdGateway(), config);
  }

  private static Map<String, String> entityMgrProps(Configuration config) {
    Map<String, String> entityMgrProps = new HashMap<>();
    config.persistenceUrl.ifPresent(
        s -> entityMgrProps.put("hibernate.connection.url", s));
    return entityMgrProps;
  }

  /**
   * Returns a CssLoaderOsdGatewayAccessLibrary object containing the default configuration.
   */
  private static StationReceiverOsdGateway osdGateway() {
    EntityManagerFactory wfEntityManager = WaveformsEntityManagerFactories.create(
        entityManagerProperties);
    EntityManagerFactory stationRefEntityManager = StationReferenceEntityManagerFactories.create(
        entityManagerProperties);
    StationReferenceRepositoryInterface stationRefRepo
        = new StationReferenceRepositoryJpa(stationRefEntityManager);

    return new StationReceiverOsdGateway(
        new StationSohRepositoryJpa(wfEntityManager),
        new WaveformRepositoryInfluxDb(wfEntityManager),
        new RawStationDataFrameRepositoryJpa(wfEntityManager),
        new ProcessingStationReferenceFactory(stationRefRepo));
  }
}
