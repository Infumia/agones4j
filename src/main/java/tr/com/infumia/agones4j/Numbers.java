package tr.com.infumia.agones4j;

import java.util.Objects;
import java.util.Optional;

/**
 * A utility class for parsing primitive numbers from strings.
 */
final class Numbers {

  /**
   * Ctor.
   */
  private Numbers() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  /**
   * Parses the given text into an integer value.
   *
   * @param text the text to parse.
   *
   * @return an {@code Optional} containing the parsed integer value if the text can be parsed, or an empty
   *   {@code Optional} if the text cannot be parsed.
   *
   * @see Integer#parseInt(String)
   * @see Optional#of(Object)
   * @see Optional#empty()
   */
  static Optional<Integer> parseInt(final String text) {
    Objects.requireNonNull(text, "text");
    try {
      return Optional.of(Integer.parseInt(text));
    } catch (final Exception ignored) {}
    return Optional.empty();
  }
}
