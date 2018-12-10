package gms.core.waveformqc.waveformqccontrol.util;

import gms.core.waveformqc.waveformqccontrol.control.ExecuteCommand;
import gms.core.waveformqc.waveformqccontrol.objects.ChannelSohSubtype;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus;
import gms.core.waveformqc.waveformqccontrol.plugin.PluginConfiguration;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.yaml.snakeyaml.Yaml;

public class TestUtility {

  public static final PluginConfiguration TEST_PLUGIN_CONFIGURATION = buildTestPluginConfiguration();

  private static PluginConfiguration buildTestPluginConfiguration() {
    InputStream pluginConfigInputStream = TestUtility.class.getClassLoader().getResourceAsStream(
        "gms/core/waveformqc/waveformqccontrol/test_plugin_config.yaml"
    );

    Yaml yaml = new Yaml();
    Object pluginConfiguration = yaml.load(pluginConfigInputStream);
    return PluginConfiguration.from((Map<String, Object>) pluginConfiguration);
  }

  public static ExecuteCommand buildCommand() {
    return buildCommand(Collections.singleton(UUID.randomUUID()));
  }

  public static ExecuteCommand buildCommand(Set<UUID> processingChannelIds) {
    ProcessingContext processingContext = ProcessingContext.createAutomatic(
        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
        StorageVisibility.PRIVATE);

    return ExecuteCommand
        .create(processingChannelIds, Instant.MIN, Instant.MAX, processingContext);
  }

  public static WaveformQcChannelSohStatus createMockWaveformQcChannelSohStatus(
      boolean startingStatus, int numStatuses, UUID processingChannelId,
      QcMaskType qcMaskType, ChannelSohSubtype sohSubtype) {

    boolean currentStatus = startingStatus;

    WaveformQcChannelSohStatus.Builder builder = WaveformQcChannelSohStatus
        .builder(processingChannelId, qcMaskType, sohSubtype, Instant.ofEpochSecond(0),
            Instant.ofEpochSecond(1), currentStatus, Duration.ofMillis(500));

    currentStatus = !currentStatus;
    for (int i = 1; i < numStatuses; i++) {
      builder
          .addStatusChange(Instant.ofEpochSecond(i), Instant.ofEpochSecond(i + 1), currentStatus);
      currentStatus = !currentStatus;
    }

    return builder.build();
  }

}
