package gms.dataacquisition.cssreader.data;

import java.time.Instant;

/**
 * A class to store a name and date, used to index items where the name is not unique.
 */
public class NameByDate {
  private final String name;
  private final Instant date;

  public NameByDate(String name, Instant date) {
    this.name = name;
    this.date = date;
  }

  public String getName() {
    return name;
  }

  public Instant getDate() {
    return date;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    NameByDate that = (NameByDate) o;

    if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) {
      return false;
    }
    return getDate() != null ? getDate().equals(that.getDate()) : that.getDate() == null;
  }

  @Override
  public int hashCode() {
    int result = getName() != null ? getName().hashCode() : 0;
    result = 31 * result + (getDate() != null ? getDate().hashCode() : 0);
    return result;
  }
}
