package tr.com.infumia.agones4j;

import agones.dev.sdk.alpha.Alpha;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public final class AgonesList {
  private final String name;
  private final long capacity;
  private final List<String> values;

  private AgonesList(final String name, final long capacity, final List<String> values) {
    this.name = name;
    this.capacity = capacity;
    this.values = Collections.unmodifiableList(values);
  }

  static AgonesList fromAgones(final Alpha.List list) {
    return new AgonesList(list.getName(), list.getCapacity(), list.getValuesList());
  }

  Alpha.List toAgones() {
    return Alpha.List.newBuilder()
      .setName(this.name)
      .setCapacity(this.capacity)
      .addAllValues(this.values)
      .build();
  }

  public String getName() {
    return this.name;
  }

  public long getCapacity() {
    return this.capacity;
  }

  public List<String> getValues() {
    return this.values;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || this.getClass() != obj.getClass()) {
      return false;
    }
    final AgonesList that = (AgonesList) obj;
    return Objects.equals(this.capacity, that.capacity) &&
      Objects.equals(this.name, that.name) &&
      Objects.equals(this.values, that.values);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.name, this.capacity, this.values);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", AgonesList.class.getSimpleName() + "[", "]")
      .add("name='" + this.name + "'")
      .add("capacity=" + this.capacity)
      .add("values=" + this.values)
      .toString();
  }
}
