package gms.core.signalenhancement.waveformfiltering.objects.dto;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import java.util.List;

public class StoreChannelSegmentsDto {

  private List<ChannelSegment> channelSegments;
  private StorageVisibility storageVisibility;

  public StoreChannelSegmentsDto() {
  }

  public StoreChannelSegmentsDto(
      List<ChannelSegment> channelSegments,
      StorageVisibility storageVisibility) {
    this.channelSegments = channelSegments;
    this.storageVisibility = storageVisibility;
  }

  public List<ChannelSegment> getChannelSegments() {
    return channelSegments;
  }

  public void setChannelSegments(
      List<ChannelSegment> channelSegments) {
    this.channelSegments = channelSegments;
  }

  public StorageVisibility getStorageVisibility() {
    return storageVisibility;
  }

  public void setStorageVisibility(
      StorageVisibility storageVisibility) {
    this.storageVisibility = storageVisibility;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    StoreChannelSegmentsDto that = (StoreChannelSegmentsDto) o;

    if (channelSegments != null ? !channelSegments.equals(that.channelSegments)
        : that.channelSegments != null) {
      return false;
    }
    return storageVisibility == that.storageVisibility;
  }

  @Override
  public int hashCode() {
    int result = channelSegments != null ? channelSegments.hashCode() : 0;
    result = 31 * result + (storageVisibility != null ? storageVisibility.hashCode() : 0);
    return result;
  }
}
