package net.infumia.agones4j;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import agones.dev.sdk.Sdk;
import agones.dev.sdk.beta.Beta;
import io.grpc.stub.StreamObserver;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

final class InternalTest {

    @Test
    void toCounterCopiesAllFields() {
        final Beta.Counter counter = Beta.Counter.newBuilder()
            .setName("rooms")
            .setCapacity(10L)
            .setCount(3L)
            .build();

        assertThat(Internal.toCounter(counter)).isEqualTo(new AgonesCounter("rooms", 10L, 3L));
    }

    @Test
    void toListCopiesAllFields() {
        final Beta.List list = Beta.List.newBuilder()
            .setName("players")
            .setCapacity(4L)
            .addAllValues(Arrays.asList("a", "b"))
            .build();

        assertThat(Internal.toList(list)).isEqualTo(
            new AgonesList("players", 4L, Arrays.asList("a", "b"))
        );
    }

    @Test
    void toAgonesListRoundTrips() {
        final AgonesList list = new AgonesList("players", 4L, Arrays.asList("a", "b"));

        final Beta.List proto = Internal.toAgonesList(list);

        assertThat(proto.getName()).isEqualTo("players");
        assertThat(proto.getCapacity()).isEqualTo(4L);
        assertThat(proto.getValuesList()).containsExactly("a", "b");
        assertThat(Internal.toList(proto)).isEqualTo(list);
    }

    @Test
    void observerToFutureCompletesWithLastValue() {
        final CompletableFuture<String> future = Internal.observerToFuture(observer -> {
            observer.onNext("first");
            observer.onNext("second");
            observer.onCompleted();
        });

        assertThat(future).isCompletedWithValue("second");
    }

    @Test
    void observerToFutureCompletesWithNullWithoutValue() {
        final CompletableFuture<String> future = Internal.observerToFuture(
            StreamObserver::onCompleted
        );

        assertThat(future).isCompletedWithValue(null);
    }

    @Test
    void observerToFutureStaysPendingUntilCompleted() {
        final CompletableFuture<String> future = Internal.observerToFuture(observer ->
            observer.onNext("value")
        );

        assertThat(future).isNotDone();
    }

    @Test
    void observerToFutureCompletesExceptionallyOnError() {
        final IllegalStateException error = new IllegalStateException("boom");

        final CompletableFuture<String> future = Internal.observerToFuture(observer ->
            observer.onError(error)
        );

        assertThat(future)
            .failsWithin(Duration.ZERO)
            .withThrowableOfType(ExecutionException.class)
            .withCause(error);
    }

    @Test
    void observerMapMapsValuesAndForwardsSignals() {
        final RecordingObserver<Integer> target = new RecordingObserver<>();
        final StreamObserver<String> mapped = Internal.observerMap(target, String::length);
        final IllegalStateException error = new IllegalStateException("boom");

        mapped.onNext("abc");
        mapped.onError(error);
        mapped.onCompleted();

        assertThat(target.values).containsExactly(3);
        assertThat(target.error).isSameAs(error);
        assertThat(target.completed).isTrue();
    }

    @Test
    void observerOnNextDispatchesToAllConsumersThroughExecutor() {
        final List<Runnable> tasks = new ArrayList<>();
        final Executor executor = tasks::add;
        final List<String> first = new ArrayList<>();
        final List<String> second = new ArrayList<>();
        final List<Consumer<String>> consumers = Arrays.asList(first::add, second::add);

        Internal.observerOnNext(executor, consumers).onNext("value");

        assertThat(first).isEmpty();
        assertThat(tasks).hasSize(1);
        tasks.get(0).run();
        assertThat(first).containsExactly("value");
        assertThat(second).containsExactly("value");
    }

    @Test
    void observerOnNextSeesConsumersAddedLater() {
        final List<Consumer<String>> consumers = new ArrayList<>();
        final StreamObserver<String> observer = Internal.observerOnNext(Runnable::run, consumers);
        final List<String> received = new ArrayList<>();

        consumers.add(received::add);
        observer.onNext("value");

        assertThat(received).containsExactly("value");
    }

    @Test
    void observerEmptyIgnoresAllSignals() {
        final StreamObserver<Sdk.Empty> observer = Internal.observerEmpty();

        assertThatCode(() -> {
            observer.onNext(Sdk.Empty.getDefaultInstance());
            observer.onError(new IllegalStateException("ignored"));
            observer.onCompleted();
        }).doesNotThrowAnyException();
    }

    private static final class RecordingObserver<T> implements StreamObserver<T> {

        private final List<T> values = new ArrayList<>();
        private Throwable error;
        private boolean completed;

        @Override
        public void onNext(final T value) {
            this.values.add(value);
        }

        @Override
        public void onError(final Throwable t) {
            this.error = t;
        }

        @Override
        public void onCompleted() {
            this.completed = true;
        }
    }
}
