package tr.com.infumia.agones4j;

import agones.dev.sdk.Sdk;
import agones.dev.sdk.beta.Beta;
import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

final class Internal {

    private static final int DEFAULT_GRPC_PORT = 9357;
    private static final String DEFAULT_GRPC_HOST = "localhost";

    private static final String ENV_GRPC_HOST = "AGONES_SDK_GRPC_HOST";
    private static final String ENV_GRPC_PORT = "AGONES_SDK_GRPC_PORT";
    private static final String ENV_GRPC_ADDRESS = "AGONES_SDK_GRPC_ADDRESS";

    static final String GRPC_HOST = Internal.getEnv(
        Internal.ENV_GRPC_HOST,
        Internal.DEFAULT_GRPC_HOST
    );
    static final int GRPC_PORT = Internal.getEnvAsInt(
        Internal.ENV_GRPC_PORT,
        Internal.DEFAULT_GRPC_PORT
    );
    static final String GRPC_ADDRESS = Internal.getEnv(Internal.ENV_GRPC_ADDRESS, null);

    private static final StreamObserver<Sdk.Empty> EMPTY = new Adapter<>();

    private Internal() {
        throw new UnsupportedOperationException(
            "This is a utility class and cannot be instantiated"
        );
    }

    static AgonesCounter toCounter(final Beta.Counter counter) {
        return new AgonesCounter(counter.getName(), counter.getCapacity(), counter.getCount());
    }

    static AgonesList toList(final Beta.List list) {
        return new AgonesList(list.getName(), list.getCapacity(), list.getValuesList());
    }

    static Beta.List toAgonesList(final AgonesList list) {
        return Beta.List.newBuilder()
            .setName(list.getName())
            .setCapacity(list.getCapacity())
            .addAllValues(list.getValues())
            .build();
    }

    private static String getEnv(final String key, final String def) {
        Objects.requireNonNull(key, "key");
        return Internal.getEnvOptional(key).orElse(def);
    }

    private static int getEnvAsInt(final String key, final int def) {
        Objects.requireNonNull(key, "key");
        return Internal.getEnvOptional(key).flatMap(Internal::parseInt).orElse(def);
    }

    private static Optional<String> getEnvOptional(final String key) {
        Objects.requireNonNull(key, "key");
        return Optional.ofNullable(Internal.getEnv(key));
    }

    private static String getEnv(final String key) {
        Objects.requireNonNull(key, "key");
        return System.getenv(key);
    }

    private static Optional<Integer> parseInt(final String text) {
        Objects.requireNonNull(text, "text");
        try {
            return Optional.of(Integer.parseInt(text));
        } catch (final Exception ignored) {}
        return Optional.empty();
    }

    static StreamObserver<Sdk.Empty> observerEmpty() {
        return Internal.EMPTY;
    }

    static <T> CompletableFuture<T> observerToFuture(final Consumer<StreamObserver<T>> response) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        response.accept(new Future<>(future));
        return future;
    }

    static <T> StreamObserver<T> observerOnNext(
        final Executor executor,
        final List<Consumer<T>> consumers
    ) {
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

    static <T, R> StreamObserver<R> observerMap(
        final StreamObserver<T> observer,
        final Function<R, T> mapper
    ) {
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
