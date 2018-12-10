package gms.core.signalenhancement.waveformfiltering.objects;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.core.signalenhancement.waveformfiltering.objects.StreamingFilterPluginParameters.Builder;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterType;
import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class StreamingFilterPluginParametersTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private RegistrationInfo linearFilterRegistrationInfo = RegistrationInfo
      .from("linearWaveformFilterPlugin", PluginVersion.from(1, 0, 0));

  @Test
  public void testBuild() {
    Builder builder = StreamingFilterPluginParameters.builder();
    builder.addFilterTypeMapping(FilterType.FIR_HAMMING, linearFilterRegistrationInfo);
    builder.addFilterTypeMapping(FilterType.IIR_BUTTERWORTH, linearFilterRegistrationInfo);

    StreamingFilterPluginParameters params = builder.build();

    assertNotNull(params);
    assertTrue(Arrays.stream(FilterType.values())
        .map(params::lookupPlugin)
        .allMatch(linearFilterRegistrationInfo::equals));
  }

  @Test
  public void testBuildMissingFilterTypeMappingExpectIllegalStateException() {
    Builder builder = StreamingFilterPluginParameters.builder();
    builder.addFilterTypeMapping(FilterType.FIR_HAMMING, linearFilterRegistrationInfo);

    exception.expect(IllegalStateException.class);
    exception.expectMessage(
        "StreamingFilterPluginParameters.Builder requires plugin RegistrationInfo mappings for all FilterTypes but is missing mappings for these FilterTypes: (IIR_BUTTERWORTH)");
    builder.build();
  }

  @Test
  public void testBuilderAddFilterTypeMappingNullFilterTypeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "StreamingFilterPluginParameters.Builder addFilterTypeMapping cannot accept null filterType");
    StreamingFilterPluginParameters.builder()
        .addFilterTypeMapping(null, linearFilterRegistrationInfo);
  }

  @Test
  public void testBuilderAddFilterTypeMappingNullRegistrationInfoExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "StreamingFilterPluginParameters.Builder addFilterTypeMapping cannot accept null registrationInfo");
    StreamingFilterPluginParameters.builder().addFilterTypeMapping(FilterType.FIR_HAMMING, null);
  }
}
