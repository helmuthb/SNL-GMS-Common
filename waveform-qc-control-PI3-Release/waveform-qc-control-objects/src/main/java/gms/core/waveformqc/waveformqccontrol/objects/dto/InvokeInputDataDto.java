package gms.core.waveformqc.waveformqccontrol.objects.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.core.waveformqc.waveformqccontrol.objects.InvokeInputData;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import java.util.Set;

/**
 * DTO containing the response body for the Waveform Qc Control OSD Gateway loadInputData
 * operation.
 */
public interface InvokeInputDataDto {

  @JsonCreator
  static InvokeInputData create(
      @JsonProperty("channelSegments") Set<ChannelSegment> channelSegments,
      @JsonProperty("qcMasks") Set<QcMask> qcMasks,
      @JsonProperty("waveformQcChannelSohStatuses") Set<WaveformQcChannelSohStatus> waveformQcChannelSohStatuses) {
    return InvokeInputData.create(channelSegments, qcMasks, waveformQcChannelSohStatuses);
  }
}
