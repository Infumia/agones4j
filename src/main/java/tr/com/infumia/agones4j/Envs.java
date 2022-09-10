package tr.com.infumia.agones4j;

import java.util.Optional;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * an interface that contains utility methods for environment variables.
 */
interface Envs {
  /**
   * gets the variable.
   *
   * @param key the key to get.
   *
   * @return environment variable.
   */
  @Nullable
  static String get(@NotNull final String key) {
    return System.getenv(key);
  }

  /**
   * gets the variable.
   *
   * @param key the key to get.
   * @param def the default to get.
   *
   * @return environment variable.
   */
  @Nullable
  @Contract("_, !null -> !null")
  static String get(@NotNull final String key, @Nullable final String def) {
    return Envs.getOptional(key).orElse(def);
  }

  /**
   * gets the variable as double.
   *
   * @param key the key to get.
   * @param def the default to get.
   *
   * @return environment variable as double.
   */
  static double getDouble(@NotNull final String key, final double def) {
    return Envs.getOptional(key).flatMap(Numbers::parseDouble).orElse(def);
  }

  /**
   * gets the variable as float.
   *
   * @param key the key to get.
   * @param def the default to get.
   *
   * @return environment variable as float.
   */
  static float getFloat(@NotNull final String key, final float def) {
    return Envs.getOptional(key).flatMap(Numbers::parseFloat).orElse(def);
  }

  /**
   * gets the variable as int.
   *
   * @param key the key to get.
   * @param def the default to get.
   *
   * @return environment variable as int.
   */
  static int getInt(@NotNull final String key, final int def) {
    return Envs.getOptional(key).flatMap(Numbers::parseInt).orElse(def);
  }

  /**
   * gets the variable as long.
   *
   * @param key the key to get.
   * @param def the default to get.
   *
   * @return environment variable as long.
   */
  static long getLong(@NotNull final String key, final long def) {
    return Envs.getOptional(key).flatMap(Numbers::parseLong).orElse(def);
  }

  /**
   * gets the variable.
   *
   * @param key the key to get.
   *
   * @return environment variable.
   */
  @NotNull
  static Optional<String> getOptional(@NotNull final String key) {
    return Optional.ofNullable(Envs.get(key));
  }

  /**
   * gets the env. or throw.
   *
   * @param key the key to get.
   *
   * @return environment variable.
   */
  @NotNull
  static String getOrThrow(@NotNull final String key) {
    return Exceptions.checkNotNull(
      Envs.get(key),
      "Env. called '%s' not found!",
      key
    );
  }

  /**
   * gets the variable as string array.
   *
   * @param key the key to get.
   * @param def the default to get.
   * @param regex the regex to get.
   *
   * @return environment variable as string array.
   */
  @Nullable
  @Contract("_, !null, _ -> !null")
  static String[] getStringArray(
    @NotNull final String key,
    @Nullable final String[] def,
    @NotNull final String regex
  ) {
    return Envs.getOptional(key).map(s -> s.split(regex)).orElse(def);
  }

  /**
   * gets the variable as string array.
   *
   * @param key the key to get.
   * @param def the default to get.
   *
   * @return environment variable as string array.
   */
  @Nullable
  @Contract("_, !null -> !null")
  static String[] getStringArray(
    @NotNull final String key,
    @Nullable final String[] def
  ) {
    return Envs.getStringArray(key, def, ",");
  }
}
