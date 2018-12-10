package gms.core.signalenhancement.waveformfiltering.objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterCausality;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterPassBandType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FilterParametersTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private final UUID processingChannelId = UUID.randomUUID();
  private final FilterDefinition filter = FilterDefinition.createFir(
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
      }, 0);
  private final RegistrationInfo pluginRegistrationInfo = RegistrationInfo
      .from("firFilter", PluginVersion.from(1, 0, 0));

  @Test
  public void testFromNullTypeProcessingChannelIdExpectIllegalArgumentException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "Error instantiating FilterParameters, processing channel ID cannot be null");
    FilterParameters.create(null, new HashMap<>());
  }

  @Test
  public void testFromNullTypeInfoListMapExpectIllegalArgumentException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Error instantiating FilterParameters, infoListMap cannot be null");
    FilterParameters.create(processingChannelId, null);
  }

  @Test
  public void testProcessingChannelIdExpectEqualsIdValue() {
    final UUID id1 = UUID
        .fromString("b38ae749-2833-4197-a8cb-4609ddd4342f");
    final UUID id2 = UUID
        .fromString("b38ae749-2833-4197-a8cb-4609ddd4342f");
    final UUID id3 = UUID
        .fromString("b38ae749-2833-4197-a8cb-4609ddd4342f");

    Map<RegistrationInfo, List<FilterDefinition>> infoListMap = new HashMap<>();

    infoListMap.put(pluginRegistrationInfo, List.of(filter));

    FilterParameters filterParameters1 = FilterParameters
        .create(id1, infoListMap);
    FilterParameters filterParameters2 = FilterParameters
        .create(id2, infoListMap);
    FilterParameters filterParameters3 = FilterParameters
        .create(id3, infoListMap);

    assertEquals(filterParameters1.getProcessingChannelId(),
        filterParameters2.getProcessingChannelId());
    assertEquals(filterParameters2.getProcessingChannelId(),
        filterParameters3.getProcessingChannelId());
  }

  @Test
  public void testProcessingChannelIdExpectNotEqualsIdValue() {
    final UUID id1 = UUID
        .fromString("04e7d88d-13ef-4e06-ab63-f81c6a170784");
    final UUID id2 = UUID
        .fromString("f66fbfc7-98a1-4e11-826b-968d80ef36eb");

    Map<RegistrationInfo, List<FilterDefinition>> infoListMap = new HashMap<>();

    infoListMap.put(pluginRegistrationInfo, List.of(filter));

    FilterParameters filterParameters1 = FilterParameters
        .create(id1, infoListMap);
    FilterParameters filterParameters2 = FilterParameters
        .create(id2, infoListMap);

    assertNotEquals(filterParameters1.getProcessingChannelId(),
        filterParameters2.getProcessingChannelId());
  }

  @Test
  public void testPluginInfoListMapExpectEqualsValue() {

    Map<RegistrationInfo, List<FilterDefinition>> infoListMap1 = new HashMap<>();
    Map<RegistrationInfo, List<FilterDefinition>> infoListMap2 = new HashMap<>();

    FilterDefinition filter1 = FilterDefinition.createFir(
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
        }, 0);

    infoListMap1.put(pluginRegistrationInfo, List.of(filter));
    infoListMap2.put(pluginRegistrationInfo, List.of(filter1));

    FilterParameters filterParameters1 = FilterParameters
        .create(processingChannelId, infoListMap1);
    FilterParameters filterParameters2 = FilterParameters
        .create(processingChannelId, infoListMap2);

    assertEquals(filterParameters1.getInfoListMap(),
        filterParameters2.getInfoListMap());
  }

  @Test
  public void testPluginInfoListMapExpectNotEqualsValue() {

    Map<RegistrationInfo, List<FilterDefinition>> infoListMap1 = new HashMap<>();
    Map<RegistrationInfo, List<FilterDefinition>> infoListMap2 = new HashMap<>();

    FilterDefinition filter1 = FilterDefinition.createFir(
        "Hamming", "FIR Hamming Bandpass 1Hz - 3Hz", FilterType.FIR_HAMMING
        , FilterPassBandType.BAND_PASS, 1.0, 3.0, 47,
        FilterSource.SYSTEM, FilterCausality.CAUSAL, false, 45.0, 0.5, new double[]{
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
        }, 0);

    infoListMap1.put(pluginRegistrationInfo, List.of(filter));
    infoListMap2.put(pluginRegistrationInfo, List.of(filter1));

    FilterParameters filterParameters1 = FilterParameters
        .create(processingChannelId, infoListMap1);
    FilterParameters filterParameters2 = FilterParameters
        .create(processingChannelId, infoListMap2);

    assertNotEquals(filterParameters1.getInfoListMap(),
        filterParameters2.getInfoListMap());
  }

  @Test
  public void testExpectEqualValue() {
    Map<RegistrationInfo, List<FilterDefinition>> infoListMap1 = new HashMap<>();
    Map<RegistrationInfo, List<FilterDefinition>> infoListMap2 = new HashMap<>();

    FilterDefinition filter1 = FilterDefinition.createFir(
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
        }, 0);

    infoListMap1.put(pluginRegistrationInfo, List.of(filter));
    infoListMap2.put(pluginRegistrationInfo, List.of(filter1));

    FilterParameters filterParameters1 = FilterParameters
        .create(processingChannelId, infoListMap1);
    FilterParameters filterParameters2 = FilterParameters
        .create(processingChannelId, infoListMap2);

    assertEquals(filterParameters1, filterParameters2);
  }

  @Test
  public void testExpectNotEqualValue() {
    Map<RegistrationInfo, List<FilterDefinition>> infoListMap1 = new HashMap<>();
    Map<RegistrationInfo, List<FilterDefinition>> infoListMap2 = new HashMap<>();

    FilterDefinition filter1 = FilterDefinition.createFir(
        "Hamming", "FIR Hamming Bandpass 1Hz - 3Hz", FilterType.FIR_HAMMING
        , FilterPassBandType.BAND_PASS, 1.0, 3.0, 47,
        FilterSource.SYSTEM, FilterCausality.CAUSAL, false, 45.0, 0.5, new double[]{
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
        }, 0);

    infoListMap1.put(pluginRegistrationInfo, List.of(filter));
    infoListMap2.put(pluginRegistrationInfo, List.of(filter1));

    FilterParameters filterParameters1 = FilterParameters
        .create(processingChannelId, infoListMap1);
    FilterParameters filterParameters2 = FilterParameters
        .create(processingChannelId, infoListMap2);

    assertNotEquals(filterParameters1, filterParameters2);
  }
}
