package tr.com.infumia.agones4j;

import agones.dev.sdk.Sdk;
import agones.dev.sdk.alpha.Alpha;
import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

final class StreamObservers {

  private static final StreamObserver<Sdk.Empty> EMPTY = new Adapter<>();

  private static final StreamObserver<Alpha.Empty> EMPTY_ALPHA = new Adapter<>();

  private StreamObservers() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  static StreamObserver<Sdk.Empty> empty() {
    return StreamObservers.EMPTY;
  }

  static StreamObserver<Alpha.Empty> emptyAlpha() {
    return StreamObservers.EMPTY_ALPHA;
  }

  static <T> CompletableFuture<T> toFuture(final Consumer<StreamObserver<T>> response) {
    final CompletableFuture<T> future = new CompletableFuture<>();
    response.accept(new Future<>(future));
    return future;
  }

  static <T> StreamObserver<T> onNext(final Executor executor, final List<Consumer<T>> consumers) {
    return new Adapter<T>() {
      @Override
      public void onNext(final T value) {
        executor.execute(() -> {
          for (final Consumer<T> consumer : consumers) {
            consumer.accept(value);
          }
        });
      }
    };
  }

  static <T, R> StreamObserver<R> map(final StreamObserver<T> observer, final Function<R, T> mapper) {
    return new Mapped<>(observer, mapper);
  }

  private static final class Mapped<T, R> implements StreamObserver<R> {
    private final StreamObserver<T> observer;
    private final Function<R, T> mapper;

    private Mapped(final StreamObserver<T> observer, final Function<R, T> mapper) {
      this.observer = observer;
      this.mapper = mapper;
    }

    @Override
    public void onNext(final R value) {
      this.observer.onNext(this.mapper.apply(value));
    }

    @Override
    public void onError(final Throwable t) {
      this.observer.onError(t);
    }

    @Override
    public void onCompleted() {
      this.observer.onCompleted();
    }
  }

  private static final class Future<T> implements StreamObserver<T> {

    private final AtomicReference<T> value = new AtomicReference<>();

    private final CompletableFuture<T> future;

    private Future(final CompletableFuture<T> future) {
      this.future = future;
    }

    @Override
    public void onNext(final T value) {
      this.value.set(value);
    }

    @Override
    public void onError(final Throwable t) {
      this.future.completeExceptionally(t);
    }

    @Override
    public void onCompleted() {
      this.future.complete(this.value.get());
    }
  }

  private static class Adapter<T> implements StreamObserver<T> {

    @Override
    public void onNext(final T value) {}

    @Override
    public void onError(final Throwable t) {}

    @Override
    public void onCompleted() {}
  }
}
