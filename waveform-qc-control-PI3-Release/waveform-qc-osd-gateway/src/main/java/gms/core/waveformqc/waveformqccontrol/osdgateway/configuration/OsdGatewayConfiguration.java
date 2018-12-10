package gms.core.waveformqc.waveformqccontrol.osdgateway.configuration;

import com.netflix.config.DynamicStringProperty;

public class OsdGatewayConfiguration {

  private final DynamicStringProperty waveformPersistenceUrl;
  private final DynamicStringProperty stationSohPersistenceUrl;
  private final DynamicStringProperty qcMaskPersistenceUrl;
  private final DynamicStringProperty provenancePersistenceUrl;

  private OsdGatewayConfiguration(DynamicStringProperty waveformPersistenceUrl,
      DynamicStringProperty stationSohPersistenceUrl,
      DynamicStringProperty qcMaskPersistenceUrl,
      DynamicStringProperty provenancePersistenceUrl) {
    this.waveformPersistenceUrl = waveformPersistenceUrl;
    this.stationSohPersistenceUrl = stationSohPersistenceUrl;
    this.qcMaskPersistenceUrl = qcMaskPersistenceUrl;
    this.provenancePersistenceUrl = provenancePersistenceUrl;
  }

  public static OsdGatewayConfiguration create() {
    DynamicStringProperty waveformPersistenceUrl = new DynamicStringProperty(
        "persistence_waveform_url",
        Defaults.WAVEFORM_PERSISTENCE_URL);

    DynamicStringProperty stationSohPersistenceUrl = new DynamicStringProperty(
        "persistence_stationSoh_url",
        Defaults.STATION_SOH_PERSISTENCE_URL);

    DynamicStringProperty qcMaskPersistenceUrl = new DynamicStringProperty("persistence_qcMask_url",
        Defaults.QC_MASK_PERSISTENCE_URL);

    DynamicStringProperty provenancePersistenceUrl = new DynamicStringProperty(
        "persistence_provenance_url",
        Defaults.PROVENANCE_PERSISTENCE_URL);

    return new OsdGatewayConfiguration(waveformPersistenceUrl, stationSohPersistenceUrl,
        qcMaskPersistenceUrl, provenancePersistenceUrl);
  }

  public String getWaveformPersistenceUrl() {
    return waveformPersistenceUrl.get();
  }

  public String getStationSohPersistenceUrl() {
    return stationSohPersistenceUrl.get();
  }

  public String getQcMaskPersistenceUrl() {
    return qcMaskPersistenceUrl.get();
  }

  public String getProvenancePersistenceUrl() {
    return provenancePersistenceUrl.get();
  }

  private static class Defaults {

    private static final String WAVEFORM_PERSISTENCE_URL
        = "jdbc:postgresql://localhost:5432/xmp_metadata";
    private static final String STATION_SOH_PERSISTENCE_URL
        = "jdbc:postgresql://localhost:5432/xmp_metadata";
    private static final String QC_MASK_PERSISTENCE_URL
        = "jdbc:postgresql://localhost:5432/xmp_metadata";
    private static final String PROVENANCE_PERSISTENCE_URL
        = "jdbc:postgresql://localhost:5432/xmp_metadata";
  }
}
