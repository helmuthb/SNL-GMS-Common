package gms.core.waveformqc.waveformqccontrol.objects.dto;

import gms.core.waveformqc.waveformqccontrol.objects.ChannelSohSubtype;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus.StatusState;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class WaveformQcChannelSohStatusDto {

  private UUID processingChannelId;
  private QcMaskType qcMaskType;
  private ChannelSohSubtype channelSohSubtype;
  private List<StatusChangeDto> statusChanges;

  protected static class StatusChangeDto {

    private Instant startTime;
    private Instant endTime;
    private StatusState status;

    public StatusChangeDto() {
    }

    public StatusChangeDto(Instant startTime, Instant endTime, StatusState status) {
      this.startTime = startTime;
      this.endTime = endTime;
      this.status = status;
    }

    public Instant getStartTime() {
      return startTime;
    }

    public Instant getEndTime() {
      return endTime;
    }

    public void setStartTime(Instant startTime) {
      this.startTime = startTime;
    }

    public void setEndTime(Instant endTime) {
      this.endTime = endTime;
    }

    public StatusState getStatus() {
      return status;
    }

    public void setStatus(StatusState status) {
      this.status = status;
    }
  }

  public WaveformQcChannelSohStatusDto() {
    // Default constructor to allow serialization of this class
  }

  public UUID getProcessingChannelId() {
    return processingChannelId;
  }

  public void setProcessingChannelId(UUID processingChannelId) {
    this.processingChannelId = processingChannelId;
  }

  public QcMaskType getQcMaskType() {
    return qcMaskType;
  }

  public void setQcMaskType(QcMaskType qcMaskType) {
    this.qcMaskType = qcMaskType;
  }

  public ChannelSohSubtype getChannelSohSubtype() {
    return channelSohSubtype;
  }

  public void setChannelSohSubtype(ChannelSohSubtype channelSohSubtype) {
    this.channelSohSubtype = channelSohSubtype;
  }

  public List<StatusChangeDto> getStatusChanges() {
    return statusChanges;
  }

  public void setStatusChanges(List<StatusChangeDto> statusChanges) {
    this.statusChanges = statusChanges;
  }
}
