package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa;

import java.util.Collections;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import org.slf4j.LoggerFactory;

/**
 * Define a class to provide access to the JPA EntityManagerFactory.
 */
public class WaveformsEntityManagerFactories {
  private static final org.slf4j.Logger logger = LoggerFactory
      .getLogger(WaveformsEntityManagerFactories.class);

  private static final String UNIT_NAME = "waveforms";

  public static EntityManagerFactory create() {
    return create(Collections.emptyMap());
  }

  public static EntityManagerFactory create(final String persistenceUnit) {
    return create(persistenceUnit, Collections.emptyMap());
  }

  public static EntityManagerFactory create(Map<String, String> propertiesOverrides) {
    return create(UNIT_NAME, propertiesOverrides);
  }

  private static EntityManagerFactory create(String persistenceUnit, Map<String, String> propertiesOverrides) {
    EntityManagerFactory factory;

    try {
      factory = Persistence.createEntityManagerFactory(persistenceUnit, propertiesOverrides);
    } catch (PersistenceException e) {
      logger.error("Unable to locate persistence unit: " + persistenceUnit, e);
      throw new IllegalArgumentException("Could not create Waveforms EntityManagerFactory",
          e);
    }

    return factory;
  }
}
