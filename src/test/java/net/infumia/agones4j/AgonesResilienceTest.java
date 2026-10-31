package net.infumia.agones4j;

import static org.assertj.core.api.Assertions.assertThat;

import agones.dev.sdk.Sdk;
import io.grpc.Status;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class AgonesResilienceTest {

    private FakeAgonesServer server;
    private ExecutorService watcherExecutor;
    private ScheduledExecutorService healthCheckExecutor;
    private Agones agones;

    @BeforeEach
    void setUp() throws Exception {
        this.server = new FakeAgonesServer();
        this.watcherExecutor = Executors.newSingleThreadExecutor();
        this.healthCheckExecutor = Executors.newSingleThreadScheduledExecutor();
        this.agones = this.newAgones();
    }

    @AfterEach
    void tearDown() throws Exception {
        this.agones.close();
        this.server.close();
        this.watcherExecutor.shutdownNow();
        this.healthCheckExecutor.shutdownNow();
    }

    @Test
    void gameServerWatchReconnectsAfterConnectionLoss() throws Exception {
        this.server.watchUpdates.add(gameServer("gs-1"));
        final BlockingQueue<Sdk.GameServer> updates = new LinkedBlockingQueue<>();
        this.agones.addGameServerWatcher(updates::add);
        assertThat(updates.poll(5L, TimeUnit.SECONDS)).isEqualTo(gameServer("gs-1"));

        this.server.watchUpdates.clear();
        this.server.watchUpdates.add(gameServer("gs-2"));
        this.server.failWatchStreams(Status.UNAVAILABLE);

        assertThat(updates.poll(5L, TimeUnit.SECONDS)).isEqualTo(gameServer("gs-2"));
        assertThat(this.server.watchCalls).hasValue(2);
    }

    @Test
    void gameServerWatchDoesNotRetryNonTransientErrors() throws Exception {
        this.agones.addGameServerWatcher(gameServer -> {});
        eventually(() -> this.server.watchCalls.get() == 1);

        this.server.failWatchStreams(Status.PERMISSION_DENIED);

        Thread.sleep(200L);
        assertThat(this.server.watchCalls).hasValue(1);
    }

    @Test
    void concurrentWatcherRegistrationOpensSingleStream() throws Exception {
        for (int round = 0; round < 20; round++) {
            final int before = this.server.watchCalls.get();
            try (Agones agones = this.newAgones()) {
                runConcurrently(8, () -> agones.addGameServerWatcher(gameServer -> {}));
                eventually(() -> this.server.watchCalls.get() > before);
                Thread.sleep(20L);
                assertThat(this.server.watchCalls.get() - before)
                    .as("round %d", round)
                    .isOne();
            }
        }
    }

    @Test
    void healthCheckingReopensStreamAfterFailure() throws Exception {
        this.agones.startHealthChecking();
        assertThat(this.server.healthPings.tryAcquire(1, 5L, TimeUnit.SECONDS)).isTrue();

        this.server.failHealthStreams(Status.UNAVAILABLE);
        this.server.healthPings.drainPermits();

        assertThat(this.server.healthPings.tryAcquire(3, 5L, TimeUnit.SECONDS)).isTrue();
        assertThat(this.server.healthStreamsOpened).hasValue(2);
    }

    @Test
    void stopHealthCheckingClosesStream() throws Exception {
        this.agones.startHealthChecking();
        assertThat(this.server.healthPings.tryAcquire(1, 5L, TimeUnit.SECONDS)).isTrue();

        this.agones.stopHealthChecking();

        eventually(() -> this.server.healthStreamsCompleted.get() == 1);
    }

    @Test
    void restartingHealthCheckingReplacesStream() throws Exception {
        this.agones.startHealthChecking();
        assertThat(this.server.healthPings.tryAcquire(1, 5L, TimeUnit.SECONDS)).isTrue();

        this.agones.startHealthChecking();
        assertThat(this.server.healthPings.tryAcquire(1, 5L, TimeUnit.SECONDS)).isTrue();

        eventually(() -> this.server.healthStreamsCompleted.get() == 1);
        assertThat(this.server.healthStreamsOpened).hasValue(2);
    }

    @Test
    void concurrentHealthCheckRestartsKeepSingleStream() throws Exception {
        runConcurrently(8, this.agones::startHealthChecking);
        assertThat(this.server.healthPings.tryAcquire(3, 5L, TimeUnit.SECONDS)).isTrue();

        this.agones.stopHealthChecking();

        eventually(
            () -> this.server.healthStreamsCompleted.get() == this.server.healthStreamsOpened.get()
        );
    }

    private Agones newAgones() {
        return Agones.builder()
            .withChannel(this.server.newChannel())
            .withGameServerWatcherExecutor(this.watcherExecutor)
            .withHealthCheckExecutor(this.healthCheckExecutor)
            .withHealthCheck(Duration.ZERO, Duration.ofMillis(10L))
            .build();
    }

    private static void runConcurrently(final int threads, final Runnable action)
        throws InterruptedException {
        final CountDownLatch start = new CountDownLatch(1);
        final List<Thread> workers = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            final Thread worker = new Thread(() -> {
                try {
                    start.await();
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                action.run();
            });
            worker.start();
            workers.add(worker);
        }
        start.countDown();
        for (final Thread worker : workers) {
            worker.join(5000L);
        }
    }

    private static void eventually(final BooleanSupplier condition) throws InterruptedException {
        final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5L);
        while (!condition.getAsBoolean()) {
            if (System.nanoTime() > deadline) {
                throw new AssertionError("Condition not met within 5 seconds");
            }
            Thread.sleep(5L);
        }
    }

    private static Sdk.GameServer gameServer(final String name) {
        return Sdk.GameServer.newBuilder()
            .setObjectMeta(Sdk.GameServer.ObjectMeta.newBuilder().setName(name))
            .build();
    }
}
