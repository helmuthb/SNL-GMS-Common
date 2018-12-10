package gms.core.signalenhancement.waveformfiltering.objects;

import gms.core.signalenhancement.waveformfiltering.objects.StreamingFilterPluginParameters.Builder;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterCausality;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterPassBandType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterType;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FilterConfiguration {

  // TODO: implement for real and add tests.

  public FilterConfiguration() {
  }

  /**
   * Obtains the {@link FilterParameters} used in claim check invocations of filter-control applications.
   * The returned parameters are an {@link Optional} since it may not be possible to create parameters for the provided processingChannelStepId.
   *
   * @param processingChannelStepId UUID to a Processing Channel Step
   * @return Optional {@link FilterParameters}
   */
  public Optional<FilterParameters> createParameters(UUID processingChannelStepId) {

    FilterDefinition filterDefinition = FilterDefinition.createFir(
        "Hamming", "FIR Hamming Bandpass 1Hz - 3Hz", FilterType.FIR_HAMMING
        , FilterPassBandType.BAND_PASS, 1.0, 3.0, 47,
        FilterSource.SYSTEM, FilterCausality.CAUSAL, false, 40.0, 0.5, new double[]{
            -0.00041931139298, -0.000674434004618, -0.00075295439347, -0.000564329029297,
            6.00603432641e-19, 0.000949341935097, 0.00206546993035, 0.00282615645799,
            0.0024372474717, -2.06057591484e-18, -0.00520585874328, -0.013391100708,
            -0.0239791261194, -0.0354872315641, -0.0456435786343, -0.051757642404, -0.0512886864221,
            -0.0424901510066, -0.0249687517076, 0, 0.0295102454011, 0.0594458360283,
            0.0852129861677, 0.102632086058, 0.108786936013, 0.102632086058, 0.0852129861677,
            0.0594458360283, 0.0295102454011, 0, -0.0249687517076, -0.0424901510066,
            -0.0512886864221, -0.051757642404, -0.0456435786343, -0.0354872315641, -0.0239791261194,
            -0.013391100708, -0.00520585874328, -2.06057591484e-18, 0.0024372474717,
            0.00282615645799, 0.00206546993035, 0.000949341935097, 6.00603432641e-19,
            -0.000564329029297, -0.00075295439347, -0.000674434004618, -0.00041931139298
        }
        , 0
    );

    RegistrationInfo firFilterPlugin = RegistrationInfo
        .from("linearWaveformFilterPlugin", PluginVersion.from(1, 0, 0));

    Map<RegistrationInfo, List<FilterDefinition>> infoListMap = Map
        .of(firFilterPlugin, List.of(filterDefinition));

    return Optional.of(FilterParameters.create(processingChannelStepId, infoListMap));
  }

  /**
   * Obtains the {@link StreamingFilterPluginParameters} used in streaming calls to filter-control
   * application.
   *
   * @return a {@link StreamingFilterPluginParameters}, not null
   */
  public StreamingFilterPluginParameters createStreamingFilterPluginParameters() {
    final RegistrationInfo linearFilterRegistrationInfo = RegistrationInfo
        .from("linearWaveformFilterPlugin", PluginVersion.from(1, 0, 0));

    Builder builder = StreamingFilterPluginParameters.builder();
    Arrays.stream(FilterType.values())
        .forEach(t -> builder.addFilterTypeMapping(t, linearFilterRegistrationInfo));

    return builder.build();
  }
}
