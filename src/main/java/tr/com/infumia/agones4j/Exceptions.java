package tr.com.infumia.agones4j;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * an interface that contains utility methods for exceptions.
 */
interface Exceptions {
  /**
   * throws an {@link IllegalArgumentException}.
   *
   * @param message the message to throw.
   * @param args the args to throw.
   *
   * @throws IllegalArgumentException anyway.
   */
  @Contract("_, _ -> fail")
  static void argument(
    @NotNull final String message,
    @NotNull final Object... args
  ) throws IllegalArgumentException {
    throw new IllegalArgumentException(message.formatted(args));
  }

  /**
   * checks if the {@code check} is false throws {@link IllegalArgumentException}.
   *
   * @param check the check to check.
   * @param message the message to check.
   * @param args the args to check
   *
   * @throws IllegalArgumentException if the {@code check} is {@code false}.
   */
  @Contract("false, _, _ -> fail")
  static void checkArgument(
    final boolean check,
    @NotNull final String message,
    @NotNull final Object... args
  ) throws IllegalArgumentException {
    if (!check) {
      Exceptions.argument(message, args);
    }
  }

  /**
   * checks if the {@code object} is null, if it's throws {@link NullPointerException}
   *
   * @param object the object to check.
   * @param message the message to check.
   * @param args the args to check.
   * @param <T> type of the object.
   *
   * @return the nonnull object.
   *
   * @throws NullPointerException if the {@code object} is null.
   */
  @NotNull
  @Contract("null, _, _ -> fail; !null, _, _ -> param1")
  static <T> T checkNotNull(
    @Nullable final T object,
    @NotNull final String message,
    @NotNull final Object... args
  ) throws NullPointerException {
    if (object == null) {
      Exceptions.nullPointer(message, args);
    }
    return object;
  }

  /**
   * checks if the {@code object} is null, if it's throws {@link NullPointerException}
   *
   * @param object the object to check.
   * @param <T> type of the object.
   *
   * @return the nonnull object.
   *
   * @throws NullPointerException if the {@code object} is null.
   */
  @NotNull
  @Contract("null -> fail; !null -> param1")
  static <T> T checkNotNull(@Nullable final T object)
    throws NullPointerException {
    if (object == null) {
      throw new NullPointerException();
    }
    return object;
  }

  /**
   * checks if the {@code check} is false throws {@link IllegalStateException}.
   *
   * @param check the check to check.
   * @param message the message to check.
   * @param args the args to check
   *
   * @throws IllegalStateException if the {@code check} is {@code false}.
   */
  @Contract("false, _, _ -> fail")
  static void checkState(
    final boolean check,
    @NotNull final String message,
    @NotNull final Object... args
  ) throws IllegalStateException {
    if (!check) {
      Exceptions.state(message, args);
    }
  }

  /**
   * throws an {@link NullPointerException}.
   *
   * @param message the message to throw.
   * @param args the args to throw.
   *
   * @throws NullPointerException anyway.
   */
  @Contract("_, _ -> fail")
  static void nullPointer(
    @NotNull final String message,
    @NotNull final Object... args
  ) throws NullPointerException {
    throw new NullPointerException(message.formatted(args));
  }

  /**
   * throws an {@link IllegalStateException}.
   *
   * @param message the message to throw.
   * @param args the args to throw.
   *
   * @throws IllegalStateException anyway.
   */
  @Contract("_, _ -> fail")
  static void state(
    @NotNull final String message,
    @NotNull final Object... args
  ) throws IllegalStateException {
    throw new IllegalStateException(message.formatted(args));
  }
}
