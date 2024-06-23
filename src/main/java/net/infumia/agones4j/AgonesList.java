package net.infumia.agones4j;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Represents a list in the Agones system.
 */
public final class AgonesList {

    private final String name;
    private final long capacity;
    private final List<String> values;

    AgonesList(final String name, final long capacity, final List<String> values) {
        this.name = name;
        this.capacity = capacity;
        this.values = Collections.unmodifiableList(values);
    }

    /**
     * Retrieves the name of the list.
     *
     * @return The name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Retrieves the capacity of the list.
     *
     * @return The capacity.
     */
    public long getCapacity() {
        return this.capacity;
    }

    /**
     * Returns the values of the list.
     *
     * @return the values.
     */
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
        return (
            Objects.equals(this.capacity, that.capacity) &&
            Objects.equals(this.name, that.name) &&
            Objects.equals(this.values, that.values)
        );
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
