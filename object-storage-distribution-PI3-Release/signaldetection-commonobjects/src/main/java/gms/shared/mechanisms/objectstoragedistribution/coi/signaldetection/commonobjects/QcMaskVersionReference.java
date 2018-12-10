package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import java.util.Objects;
import java.util.UUID;

/**
 * A reference to a particular {@link QcMaskVersion} from a {@link QcMask}
 */
public class QcMaskVersionReference {

  private final UUID qcMaskId;
  private final long qcMaskVersionId;

  /**
   * Obtains an instance of QcMaskVersionReference for a given {@link QcMask} and its {@link
   * QcMaskVersion}.
   *
   * @param qcMaskId The unique identifier to the {@link QcMask}.
   * @param qcMaskVersionId The unique identifier to the {@link QcMaskVersion}.
   */
  private QcMaskVersionReference(UUID qcMaskId,
      long qcMaskVersionId) {

    this.qcMaskId = qcMaskId;
    this.qcMaskVersionId = qcMaskVersionId;
  }

  /**
   * Creates a {@link QcMaskVersionReference} from the provided {@link QcMask} id and {@link
   * QcMaskVersion} version value.
   *
   * @param qcMaskId QcMask Identity, not null
   * @param qcMaskVersionId QcMaskVersion Identity, not null
   * @return QcMaskVersionReference to the qcMaskId and qcMaskVersionId, not null
   * @throws IllegalArgumentException if either of the input arguments are null
   */
  public static QcMaskVersionReference from(UUID qcMaskId, long qcMaskVersionId) {

    Objects.requireNonNull(qcMaskId, "QCMaskVersionReference's qcMaskId cannot be null");
    Objects.requireNonNull(qcMaskVersionId,
        "QCMaskVersionReference's qcMaskVersionId cannot be null");

    return new QcMaskVersionReference(qcMaskId, qcMaskVersionId);
  }

  /**
   * Gets the {@link UUID} from this QcMaskVersionReference's {@link QcMask}
   *
   * @return identity to a QcMask, not null
   */
  public UUID getQcMaskId() {
    return this.qcMaskId;
  }

  /**
   * Gets the version from this QcMaskVersionReference's {@link QcMaskVersion}
   *
   * @return identity to a QcMaskVersion, not null
   */
  public long getQcMaskVersionId() {
    return this.qcMaskVersionId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    QcMaskVersionReference reference = (QcMaskVersionReference) o;
    return qcMaskVersionId == reference.qcMaskVersionId &&
        Objects.equals(qcMaskId, reference.qcMaskId);
  }

  @Override
  public int hashCode() {

    return Objects.hash(qcMaskId, qcMaskVersionId);
  }

  @Override
  public String toString() {
    return "QcMaskVersionReference{" +
        "qcMaskId=" + qcMaskId +
        ", qcMaskVersionId=" + qcMaskVersionId +
        '}';
  }
}
