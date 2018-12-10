package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa;

import static org.junit.Assert.assertEquals;

import javax.persistence.EntityManagerFactory;
import org.junit.Test;

/**
 * Unit tests for creating JPA WaveformsEntityManagerFactories.  Most of this functionality requires
 * integration tests since the {@link EntityManagerFactory} cannot be constructed if the
 * underlying database (e.g. PostgreSQL) is not accessible.
 */
public class WaveformsEntityManagerFactoriesTests {
  @Test
  public void testCreatePersistenceUnitOverrides() {
    EntityManagerFactory factory = WaveformsEntityManagerFactories.create("waveforms-unitDB");
    assertEquals("waveforms-unitDB", factory.getProperties().get("hibernate.ejb.persistenceUnitName"));
  }
}
