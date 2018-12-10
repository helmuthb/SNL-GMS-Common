package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterCausality;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterPassBandType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterType;

public interface FilterDefinitionDto {

  @JsonCreator
  static FilterDefinition from(
      @JsonProperty("name") String name,
      @JsonProperty("description") String description,
      @JsonProperty("filterType") FilterType filterType,
      @JsonProperty("filterPassBandType") FilterPassBandType filterPassBandType,
      @JsonProperty("lowFrequencyHz") double lowFrequencyHz,
      @JsonProperty("highFrequencyHz") double highFrequencyHz,
      @JsonProperty("order") int order,
      @JsonProperty("filterSource") FilterSource filterSource,
      @JsonProperty("filterCausality") FilterCausality filterCausality,
      @JsonProperty("zeroPhase") boolean zeroPhase,
      @JsonProperty("sampleRate") double sampleRate,
      @JsonProperty("sampleRateTolerance") double sampleRateTolerance,
      @JsonProperty("aCoefficients") double[] aCoefficients,
      @JsonProperty("bCoefficients") double[] bCoefficients,
      @JsonProperty("groupDelaySecs") double groupDelaySecs) {

    // Note: Jackson doesn't actually use the body during mixin processing.  This is here
    // to avoid compiler errors
    return FilterDefinition
        .from(name, description, filterType, filterPassBandType, lowFrequencyHz, highFrequencyHz,
            order, filterSource, filterCausality, zeroPhase, sampleRate, sampleRateTolerance,
            aCoefficients, bCoefficients, groupDelaySecs);

  }

  // Without this annotation the field serializes as "bcoefficients" instead of "bCoefficients"
  @JsonProperty("bCoefficients")
  double[] getBCoefficients();

  // Without this annotation the field serializes as "acoefficients" instead of "aCoefficients"
  @JsonProperty("aCoefficients")
  double[] getACoefficients();
}
