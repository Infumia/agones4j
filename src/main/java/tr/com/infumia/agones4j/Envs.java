package tr.com.infumia.agones4j;

import java.util.Objects;
import java.util.Optional;

/**
 * A utility class for retrieving environment variables.
 */
final class Envs {

  /**
   * Ctor.
   */
  private Envs() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  /**
   * Retrieves the value of the specified environment variable.
   *
   * @param key the name of the environment variable
   * @param def the default value to return if the environment variable is not found
   *
   * @return the value of the environment variable, or the default value if the variable is not found
   *
   * @throws NullPointerException if the key is null
   */
  static String get(final String key, final String def) {
    Objects.requireNonNull(key, "key");
    return Envs.getOptional(key).orElse(def);
  }

  /**
   * Retrieves the value of the specified environment variable and converts it to an integer.
   *
   * @param key the name of the environment variable
   * @param def the default value to return if the environment variable is not found
   *
   * @return the value of the environment variable, or the default value if the variable is not found
   *
   * @throws NullPointerException if the key is null
   */
  static int getInt(final String key, final int def) {
    Objects.requireNonNull(key, "key");
    return Envs.getOptional(key).flatMap(Numbers::parseInt).orElse(def);
  }

  /**
   * Retrieves the value of the specified environment variable.
   *
   * @param key the name of the environment variable
   *
   * @return the value of the environment variable, or null if the variable is not found
   *
   * @throws NullPointerException if the key is null
   */
  private static Optional<String> getOptional(final String key) {
    Objects.requireNonNull(key, "key");
    return Optional.ofNullable(Envs.get(key));
  }

  /**
   * Retrieves the value of the specified environment variable.
   *
   * @param key the name of the environment variable
   *
   * @return the value of the environment variable, or null if the variable is not found
   *
   * @throws NullPointerException if the key is null
   */
  private static String get(final String key) {
    Objects.requireNonNull(key, "key");
    return System.getenv(key);
  }
}
