package gms.core.waveformqc.waveformqccontrol.objects.dto;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInformation;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import java.util.List;

/**
 * DTO object providing the QC Masks that need to be stored, and the visibility level at which to
 * store them.
 */
public class StoreQcMasksDto {

  private List<QcMask> qcMasks;
  private List<CreationInformation> creationInfos;
  private StorageVisibility storageVisibility;

  public StoreQcMasksDto() {
  }

  public StoreQcMasksDto(List<QcMask> qcMasks, List<CreationInformation> creationInfos,
      StorageVisibility storageVisibility) {
    this.qcMasks = qcMasks;
    this.creationInfos = creationInfos;
    this.storageVisibility = storageVisibility;
  }

  public List<QcMask> getQcMasks() {
    return qcMasks;
  }

  public List<CreationInformation> getCreationInfos() {
    return creationInfos;
  }

  public StorageVisibility getStorageVisibility() {
    return storageVisibility;
  }

  public void setQcMasks(List<QcMask> qcMasks) {
    this.qcMasks = qcMasks;
  }

  public void setCreationInfos(List<CreationInformation> creationInfos) {
    this.creationInfos = creationInfos;
  }

  public void setStorageVisibility(StorageVisibility storageVisibility) {
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

    StoreQcMasksDto that = (StoreQcMasksDto) o;

    return qcMasks.equals(that.qcMasks) && creationInfos.equals(that.creationInfos)
        && storageVisibility == that.storageVisibility;
  }

  @Override
  public int hashCode() {
    int result = qcMasks.hashCode();
    result = 31 * result + creationInfos.hashCode();
    result = 31 * result + storageVisibility.hashCode();
    return result;
  }
}
