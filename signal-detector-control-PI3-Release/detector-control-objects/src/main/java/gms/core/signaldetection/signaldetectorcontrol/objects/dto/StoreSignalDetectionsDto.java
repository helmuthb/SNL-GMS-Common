package gms.core.signaldetection.signaldetectorcontrol.objects.dto;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import java.util.Set;

public class StoreSignalDetectionsDto {

  private Set<SignalDetection> signalDetections;
  private StorageVisibility storageVisibility;

  public StoreSignalDetectionsDto() {
  }

  public StoreSignalDetectionsDto(Set<SignalDetection> signalDetections,
      StorageVisibility storageVisibility) {
    this.signalDetections = signalDetections;
    this.storageVisibility = storageVisibility;
  }

  public Set<SignalDetection> getSignalDetections() {
    return signalDetections;
  }

  public void setSignalDetections(Set<SignalDetection> signalDetections) {
    this.signalDetections = signalDetections;
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

    StoreSignalDetectionsDto that = (StoreSignalDetectionsDto) o;

    return (signalDetections != null ? signalDetections.equals(that.signalDetections)
        : that.signalDetections == null) && storageVisibility == that.storageVisibility;
  }

  @Override
  public int hashCode() {
    int result = signalDetections != null ? signalDetections.hashCode() : 0;
    result = 31 * result + (storageVisibility != null ? storageVisibility.hashCode() : 0);
    return result;
  }
}
