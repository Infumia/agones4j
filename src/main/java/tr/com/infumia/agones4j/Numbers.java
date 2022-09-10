package tr.com.infumia.agones4j;

import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * an interface that contains utility methods for numbers.
 */
interface Numbers {
  /**
   * parses the text into {@code double}.
   *
   * @param text the text to parse.
   *
   * @return parsed double.
   */
  @NotNull
  static Optional<Double> parseDouble(@NotNull final String text) {
    try {
      return Optional.of(Double.parseDouble(text));
    } catch (final Exception ignored) {}
    return Optional.empty();
  }

  /**
   * parses the text into {@code float}.
   *
   * @param text the text to parse.
   *
   * @return parsed float.
   */
  @NotNull
  static Optional<Float> parseFloat(@NotNull final String text) {
    try {
      return Optional.of(Float.parseFloat(text));
    } catch (final Exception ignored) {}
    return Optional.empty();
  }

  /**
   * parses the text into {@code int}.
   *
   * @param text the text to parse.
   *
   * @return parsed int.
   */
  @NotNull
  static Optional<Integer> parseInt(@NotNull final String text) {
    try {
      return Optional.of(Integer.parseInt(text));
    } catch (final Exception ignored) {}
    return Optional.empty();
  }

  /**
   * parses the text into {@code long}.
   *
   * @param text the text to parse.
   *
   * @return parsed long.
   */
  @NotNull
  static Optional<Long> parseLong(@NotNull final String text) {
    try {
      return Optional.of(Long.parseLong(text));
    } catch (final Exception ignored) {}
    return Optional.empty();
  }
}
