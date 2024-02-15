package tr.com.infumia.agones4j;

import agones.dev.sdk.alpha.Alpha;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public final class AgonesCounter {
  private final String name;
  private final long capacity;
  private final long count;

  private AgonesCounter(final String name, final long capacity, final long count) {
    this.name = name;
    this.capacity = capacity;
    this.count = count;
  }
  static AgonesCounter fromAgones(final Alpha.Counter counter) {
    return new AgonesCounter(counter.getName(), counter.getCapacity(), counter.getCount());
  }

  public String getName() {
    return this.name;
  }

  public long getCapacity() {
    return this.capacity;
  }

  public long getCount() {
    return this.count;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || this.getClass() != obj.getClass()) {
      return false;
    }
    final AgonesCounter that = (AgonesCounter) obj;
    return Objects.equals(this.capacity, that.capacity) &&
      Objects.equals(this.name, that.name) &&
      Objects.equals(this.count, that.count);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.name, this.capacity, this.count);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", AgonesCounter.class.getSimpleName() + "[", "]")
      .add("name='" + this.name + "'")
      .add("capacity=" + this.capacity)
      .add("count=" + this.count)
      .toString();
  }
}
