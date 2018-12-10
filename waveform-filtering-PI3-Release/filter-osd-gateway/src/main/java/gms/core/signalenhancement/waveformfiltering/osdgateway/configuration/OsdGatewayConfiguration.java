package gms.core.signalenhancement.waveformfiltering.osdgateway.configuration;

import java.util.Objects;
import java.util.function.BiPredicate;

public class OsdGatewayConfiguration {

  static final String DEFAULT_PERSISTENCE_WAVEFORM_URL = "jdbc:postgresql://localhost:5432/xmp_metadata";
  static final String DEFAULT_PROVENANCE_PERSISTENCE_URL = "jdbc:postgresql://localhost:5432/xmp_metadata";

  private final String waveformPersistenceUrl;
  private final String provenancePersistenceUrl;

  private OsdGatewayConfiguration(String waveformPersistenceUrl,
      String provenancePersistenceUrl) {
    this.waveformPersistenceUrl = waveformPersistenceUrl;
    this.provenancePersistenceUrl = provenancePersistenceUrl;
  }

  public String getWaveformPersistenceUrl() {
    return waveformPersistenceUrl;
  }

  public String getProvenancePersistenceUrl() {
    return provenancePersistenceUrl;
  }

  /**
   * Obtains an instance of {@link Builder}
   *
   * @return a Builder, not null
   */
  public static Builder builder() {
    return new Builder();
  }

  private static <A, B> void validateParameter(BiPredicate<A, B> test, A a, B b, String message) {
    if (test.test(a, b)) {
      throw new IllegalArgumentException(message);
    }
  }

  /**
   * Constructs instances of {@link HttpServiceConfiguration} following the builder pattern
   */
  public static class Builder {

    private String waveformPersistenceUrl;
    private String provenancePersistenceUrl;

    private Builder() {
      this.waveformPersistenceUrl = DEFAULT_PERSISTENCE_WAVEFORM_URL;
      this.provenancePersistenceUrl = DEFAULT_PROVENANCE_PERSISTENCE_URL;
    }

    /**
     * Construct the {@link HttpServiceConfiguration}
     *
     * @return HttpServiceConfiguration built from this {@link Builder}, not null
     * @throws IllegalArgumentException if minThreads, maxThreads, idleTimeOutMillis, or port are
     * negative; if minThreads is greater than maxThreads; if port is beyond the valid range
     */
    public OsdGatewayConfiguration build() {
      return new OsdGatewayConfiguration(waveformPersistenceUrl, provenancePersistenceUrl);

    }

    public Builder setWaveformPersistenceUrl(String waveformPersistenceUrl) {
      this.waveformPersistenceUrl = Objects.requireNonNull(waveformPersistenceUrl);
      return this;
    }

    public Builder setProvenancePersistenceUrl(String provenancePersistenceUrl) {
      this.provenancePersistenceUrl = Objects.requireNonNull(provenancePersistenceUrl);
      return this;
    }
  }
}
