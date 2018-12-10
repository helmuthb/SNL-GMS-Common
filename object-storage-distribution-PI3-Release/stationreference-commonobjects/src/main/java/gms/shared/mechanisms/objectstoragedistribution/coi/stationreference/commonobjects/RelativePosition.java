package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects;

/**
 * This class contains position information relative to a known reference point.
 */
public final class RelativePosition {

  private final double northDisplacement;
  private final double eastDisplacement;
  private final double verticalDisplacement;

  /**
   * Create a new RelativePosition object.
   * @param north The number of units north or south from the reference point.
   * @param east The number of units east or west from the reference point.
   * @param vertical The number of units up or down from the reference point.
   * @return A new RelativePosition object.
   */
  public static RelativePosition create(double north, double east, double vertical) {
    return new RelativePosition(north, east, vertical);
  }

  /**
   * Create a new RelativePosition object from existing data.
   * @param north The number of units north or south from the reference point.
   * @param east The number of units east or west from the reference point.
   * @param vertical The number of units up or down from the reference point.
   * @return A new RelativePosition object.
   */

  public static RelativePosition from(double north, double east, double vertical) {
    return new RelativePosition(north, east, vertical);
  }


  /**
   * Private constructor.

   * @param north The number of units north or south from the reference point.
   * @param east The number of units east or west from the reference point.
   * @param vertical The number of units up or down from the reference point.
   */
  private RelativePosition(double north, double east, double vertical) {

    this.northDisplacement = north;
    this.eastDisplacement = east;
    this.verticalDisplacement = vertical;
  }


  public double getNorthDisplacement() {
    return northDisplacement;
  }

  public double getEastDisplacement() {
    return eastDisplacement;
  }

  public double getVerticalDisplacement() {
    return verticalDisplacement;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    RelativePosition that = (RelativePosition) o;

    if (Double.compare(that.getNorthDisplacement(), getNorthDisplacement()) != 0) {
      return false;
    }
    if (Double.compare(that.getEastDisplacement(), getEastDisplacement()) != 0) {
      return false;
    }
    return Double.compare(that.getVerticalDisplacement(), getVerticalDisplacement()) == 0;

  }

  @Override
  public int hashCode() {
    int result = 31;
    long temp;

    temp = Double.doubleToLongBits(getNorthDisplacement());
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(getEastDisplacement());
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(getVerticalDisplacement());
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return "RelativePosition{"
        + ", north=" + this.northDisplacement
        + ", east=" + this.eastDisplacement
        + ", vertical=" + this.verticalDisplacement
        + "}";
  }

}
