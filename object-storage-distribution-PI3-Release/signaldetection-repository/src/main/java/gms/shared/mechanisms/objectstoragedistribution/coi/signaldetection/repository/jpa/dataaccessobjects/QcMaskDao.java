package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects;

import java.util.Objects;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Dao equivalent of {@link gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask},
 * used to perform storage and retrieval operations on the QcMask via JPA.
 */
@Entity
@Table(name = "qcmasks")
public class QcMaskDao {

  @Id
  @GeneratedValue
  private long daoId;

  @Column(columnDefinition = "uuid", updatable = false)
  private UUID id;

  @Column(columnDefinition = "uuid", updatable = false)
  private UUID processingChannelId;

  public QcMaskDao() {
  }

  public long getDaoId() {
    return daoId;
  }

  public void setDaoId(long daoId) {
    this.daoId = daoId;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getProcessingChannelId() {
    return processingChannelId;
  }

  public void setProcessingChannelId(UUID processingChannelId) {
    this.processingChannelId = processingChannelId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    QcMaskDao qcMaskDao = (QcMaskDao) o;
    return daoId == qcMaskDao.daoId &&
        Objects.equals(id, qcMaskDao.id) &&
        Objects.equals(processingChannelId, qcMaskDao.processingChannelId);
  }

  @Override
  public int hashCode() {

    return Objects.hash(daoId, id, processingChannelId);
  }

  @Override
  public String toString() {
    return "QcMaskDao{" +
        "daoId=" + daoId +
        ", id=" + id +
        ", processingChannelId=" + processingChannelId +
        '}';
  }
}
