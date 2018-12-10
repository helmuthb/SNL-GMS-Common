package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Define a class which represents a network, which is a collection of monitoring stations.
 */
public final class ReferenceNetwork {

  private final UUID entityId;
  private final UUID versionId;
  private final String name;
  private final String description;
  private final NetworkOrganization organization;  // monitoring organization
  private final NetworkRegion region;        // geographic region
  private final String comment;
  private final InformationSource source;        // source of the information
  private final Instant actualChangeTime;  // time information was created
  private final Instant systemChangeTime;  // time the information was entered into the system

  /**
   * Create a new ReferenceNetwork object.
   * @param name The name of the network.
   * @param org The monitoring organization.
   * @param region The geographic region.
   * @param source The source of the information.
   * @param comment Comments.
   * @param actualChangeTime The time when this information was created.
   * @return A new ReferenceNetwork object.
   * @throws NullPointerException
   */
  public static ReferenceNetwork create(String name, String description, NetworkOrganization org,
      NetworkRegion region, InformationSource source, String comment, Instant actualChangeTime)
      throws NullPointerException  {

    return new ReferenceNetwork(UUID.randomUUID(), UUID.randomUUID(), name,
        description, org, region, source, comment,
        actualChangeTime, Instant.now());
  }

  /**
   * Create a new ReferenceNetwork object, as a version of an existing entity.
   *
   * @param entityId the id of the entity this new version is for
   * @param name The name of the network.
   * @param org The monitoring organization.
   * @param region The geographic region.
   * @param source The source of the information.
   * @param comment Comments.
   * @param actualChangeTime The time when this information was created.
   * @return A new ReferenceNetwork object.
   * @throws NullPointerException
   */
  public static ReferenceNetwork createNewVersion(UUID entityId, String name,
      String description, NetworkOrganization org, NetworkRegion region, InformationSource source,
      String comment, Instant actualChangeTime)
      throws NullPointerException  {

    return new ReferenceNetwork(entityId, UUID.randomUUID(), name, description,
        org, region, source, comment, actualChangeTime, Instant.now());
  }

  /**
   * Create a ReferenceNetwork object from existing data.
   * @param entityId The id of the entity.
   * @param versionId the id of the version of the entity
   * @param name The name of the network.
   * @param org The monitoring organization.
   * @param region The geographic region.
   * @param source The source of the information.
   * @param comment Comments.
   * @param actualChangeTime The time when this information was created.
   * @param systemChangeTime The time the information was added to the system.
   * @return A ReferenceNetwork object.
   * @throws NullPointerException
   */
  public static ReferenceNetwork from(UUID entityId, UUID versionId,
      String name, String description, NetworkOrganization org,
      NetworkRegion region, InformationSource source, String comment, Instant actualChangeTime,
      Instant systemChangeTime) throws NullPointerException {

    return new ReferenceNetwork(entityId, versionId, name, description, org,
        region, source, comment, actualChangeTime, systemChangeTime);
  }

  // Private constructor.
  private ReferenceNetwork(UUID entityId, UUID versionId, String name, String description,
      NetworkOrganization org, NetworkRegion region,
      InformationSource source, String comment, Instant actualChangeTime, Instant systemChangeTime)
      throws NullPointerException {

    this.entityId = Objects.requireNonNull(entityId);
    this.versionId = Objects.requireNonNull(versionId);
    this.name = Objects.requireNonNull(name).toUpperCase().trim();
    this.description = Objects.requireNonNull(description);
    this.organization = Objects.requireNonNull(org);
    this.region = Objects.requireNonNull(region);
    this.source = Objects.requireNonNull(source);
    this.comment = Objects.requireNonNull(comment);
    this.actualChangeTime = Objects.requireNonNull(actualChangeTime);
    this.systemChangeTime = Objects.requireNonNull(systemChangeTime);
  }

  public UUID getEntityId() {
    return entityId;
  }

  public UUID getVersionId() { return versionId; }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public NetworkOrganization getOrganization() {
    return organization;
  }

  public NetworkRegion getRegion() {
    return region;
  }

  public String getComment() {
    return comment;
  }

  public InformationSource getSource() {
    return source;
  }

  public Instant getActualChangeTime() {
    return actualChangeTime;
  }

  public Instant getSystemChangeTime() {
    return systemChangeTime;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ReferenceNetwork that = (ReferenceNetwork) o;

    if (entityId != null ? !entityId.equals(that.entityId) : that.entityId != null) {
      return false;
    }
    if (versionId != null ? !versionId.equals(that.versionId) : that.versionId != null) {
      return false;
    }
    if (name != null ? !name.equals(that.name) : that.name != null) {
      return false;
    }
    if (description != null ? !description.equals(that.description) : that.description != null) {
      return false;
    }
    if (organization != that.organization) {
      return false;
    }
    if (region != that.region) {
      return false;
    }
    if (comment != null ? !comment.equals(that.comment) : that.comment != null) {
      return false;
    }
    if (source != null ? !source.equals(that.source) : that.source != null) {
      return false;
    }
    if (actualChangeTime != null ? !actualChangeTime.equals(that.actualChangeTime)
        : that.actualChangeTime != null) {
      return false;
    }
    return systemChangeTime != null ? systemChangeTime.equals(that.systemChangeTime)
        : that.systemChangeTime == null;
  }

  @Override
  public int hashCode() {
    int result = entityId != null ? entityId.hashCode() : 0;
    result = 31 * result + (versionId != null ? versionId.hashCode() : 0);
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (description != null ? description.hashCode() : 0);
    result = 31 * result + (organization != null ? organization.hashCode() : 0);
    result = 31 * result + (region != null ? region.hashCode() : 0);
    result = 31 * result + (comment != null ? comment.hashCode() : 0);
    result = 31 * result + (source != null ? source.hashCode() : 0);
    result = 31 * result + (actualChangeTime != null ? actualChangeTime.hashCode() : 0);
    result = 31 * result + (systemChangeTime != null ? systemChangeTime.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ReferenceNetwork{" +
        "entityId=" + entityId +
        ", versionId=" + versionId +
        ", name='" + name + '\'' +
        ", description='" + description + '\'' +
        ", organization=" + organization +
        ", region=" + region +
        ", comment='" + comment + '\'' +
        ", source=" + source +
        ", actualChangeTime=" + actualChangeTime +
        ", systemChangeTime=" + systemChangeTime +
        '}';
  }
}
