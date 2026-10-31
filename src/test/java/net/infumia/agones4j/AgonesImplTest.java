package net.infumia.agones4j;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import agones.dev.sdk.Sdk;
import agones.dev.sdk.alpha.Alpha;
import agones.dev.sdk.beta.Beta;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class AgonesImplTest {

    private FakeAgonesServer server;
    private Agones agones;
    private ExecutorService watcherExecutor;
    private ScheduledExecutorService healthCheckExecutor;

    @BeforeEach
    void setUp() throws Exception {
        this.server = new FakeAgonesServer();
        this.watcherExecutor = Executors.newSingleThreadExecutor();
        this.healthCheckExecutor = Executors.newSingleThreadScheduledExecutor();
        this.agones = Agones.builder()
            .withChannel(this.server.newChannel())
            .withGameServerWatcherExecutor(this.watcherExecutor)
            .withHealthCheckExecutor(this.healthCheckExecutor)
            .withHealthCheck(Duration.ZERO, Duration.ofMillis(10L))
            .build();
    }

    @AfterEach
    void tearDown() throws Exception {
        this.agones.stopHealthChecking();
        // Closing the server first cancels open health and watch streams,
        // otherwise channel shutdown waits for them until its timeout.
        this.server.close();
        this.agones.close();
        this.watcherExecutor.shutdownNow();
        this.healthCheckExecutor.shutdownNow();
    }

    @Test
    void readySendsRequest() throws Exception {
        assertThat(await(this.agones.readyFuture())).isEqualTo(Sdk.Empty.getDefaultInstance());
        assertThat(this.server.takeRequest(Sdk.Empty.class)).isNotNull();
    }

    @Test
    void allocateAndShutdownSendRequests() throws Exception {
        await(this.agones.allocateFuture());
        this.server.takeRequest(Sdk.Empty.class);

        await(this.agones.shutdownFuture());
        this.server.takeRequest(Sdk.Empty.class);
    }

    @Test
    void reserveSendsDurationInSeconds() throws Exception {
        await(this.agones.reserveFuture(Duration.ofMinutes(2L)));

        assertThat(this.server.takeRequest(Sdk.Duration.class).getSeconds()).isEqualTo(120L);
    }

    @Test
    void setLabelSendsKeyValue() throws Exception {
        await(this.agones.setLabelFuture("mode", "ranked"));

        final Sdk.KeyValue request = this.server.takeRequest(Sdk.KeyValue.class);
        assertThat(request.getKey()).isEqualTo("mode");
        assertThat(request.getValue()).isEqualTo("ranked");
    }

    @Test
    void setAnnotationSendsKeyValue() throws Exception {
        await(this.agones.setAnnotationFuture("map", "dust2"));

        final Sdk.KeyValue request = this.server.takeRequest(Sdk.KeyValue.class);
        assertThat(request.getKey()).isEqualTo("map");
        assertThat(request.getValue()).isEqualTo("dust2");
    }

    @Test
    void getGameServerReturnsServerResponse() throws Exception {
        this.server.gameServer = gameServer("gs-1");

        assertThat(await(this.agones.getGameServerFuture())).isEqualTo(gameServer("gs-1"));
    }

    @Test
    void serverErrorCompletesFutureExceptionally() {
        this.server.failure = Status.NOT_FOUND.withDescription("missing");

        assertThatThrownBy(() -> await(this.agones.getCounterFuture("rooms")))
            .isInstanceOf(ExecutionException.class)
            .cause()
            .isInstanceOf(StatusRuntimeException.class)
            .hasMessageContaining("NOT_FOUND")
            .hasMessageContaining("missing");
    }

    @Test
    void getCounterMapsResponse() throws Exception {
        this.server.counter = counter("rooms", 10L, 3L);

        assertThat(await(this.agones.getCounterFuture("rooms"))).isEqualTo(
            new AgonesCounter("rooms", 10L, 3L)
        );
        assertThat(this.server.takeRequest(Beta.GetCounterRequest.class).getName()).isEqualTo(
            "rooms"
        );
    }

    @Test
    void increaseCounterAlwaysSendsPositiveDiff() throws Exception {
        await(this.agones.increaseCounterFuture("rooms", 2L));
        await(this.agones.increaseCounterFuture("rooms", -3L));

        assertThat(this.takeCounterUpdate().getCountDiff()).isEqualTo(2L);
        assertThat(this.takeCounterUpdate().getCountDiff()).isEqualTo(3L);
    }

    @Test
    void decreaseCounterAlwaysSendsNegativeDiff() throws Exception {
        await(this.agones.decreaseCounter("rooms", 2L));
        await(this.agones.decreaseCounter("rooms", -3L));

        assertThat(this.takeCounterUpdate().getCountDiff()).isEqualTo(-2L);
        assertThat(this.takeCounterUpdate().getCountDiff()).isEqualTo(-3L);
    }

    @Test
    void setCounterCountSendsOnlyCount() throws Exception {
        this.server.counter = counter("rooms", 10L, 7L);

        assertThat(await(this.agones.setCounterCountFuture("rooms", 7L))).isEqualTo(
            new AgonesCounter("rooms", 10L, 7L)
        );
        final Beta.CounterUpdateRequest update = this.takeCounterUpdate();
        assertThat(update.getName()).isEqualTo("rooms");
        assertThat(update.hasCount()).isTrue();
        assertThat(update.getCount().getValue()).isEqualTo(7L);
        assertThat(update.hasCapacity()).isFalse();
        assertThat(update.getCountDiff()).isZero();
    }

    @Test
    void setCounterCapacitySendsOnlyCapacity() throws Exception {
        await(this.agones.setCounterCapacityFuture("rooms", 20L));

        final Beta.CounterUpdateRequest update = this.takeCounterUpdate();
        assertThat(update.hasCapacity()).isTrue();
        assertThat(update.getCapacity().getValue()).isEqualTo(20L);
        assertThat(update.hasCount()).isFalse();
        assertThat(update.getCountDiff()).isZero();
    }

    @Test
    void getListMapsResponse() throws Exception {
        this.server.list = list("players", 4L, "a", "b");

        assertThat(await(this.agones.getList("players"))).isEqualTo(
            new AgonesList("players", 4L, Arrays.asList("a", "b"))
        );
        assertThat(this.server.takeRequest(Beta.GetListRequest.class).getName()).isEqualTo(
            "players"
        );
    }

    @Test
    void addListSendsValue() throws Exception {
        await(this.agones.addListFuture("players", "a"));

        final Beta.AddListValueRequest request = this.server.takeRequest(
            Beta.AddListValueRequest.class
        );
        assertThat(request.getName()).isEqualTo("players");
        assertThat(request.getValue()).isEqualTo("a");
    }

    @Test
    void removeListSendsValue() throws Exception {
        await(this.agones.removeListFuture("players", "a"));

        final Beta.RemoveListValueRequest request = this.server.takeRequest(
            Beta.RemoveListValueRequest.class
        );
        assertThat(request.getName()).isEqualTo("players");
        assertThat(request.getValue()).isEqualTo("a");
    }

    @Test
    void updateListSendsListAndMask() throws Exception {
        final AgonesList list = new AgonesList("players", 8L, Arrays.asList("a", "b"));

        await(this.agones.updateList(list, Arrays.asList("capacity", "values")));

        final Beta.UpdateListRequest request = this.server.takeRequest(
            Beta.UpdateListRequest.class
        );
        assertThat(request.getList()).isEqualTo(list("players", 8L, "a", "b"));
        assertThat(request.getUpdateMask().getPathsList()).containsExactly("capacity", "values");
    }

    @Test
    void playerConnectSendsPlayerIdAndMapsResult() throws Exception {
        this.server.playerResult = true;

        assertThat(await(this.agones.playerConnectFuture("p1"))).isTrue();
        assertThat(this.server.takeRequest(Alpha.PlayerID.class).getPlayerID()).isEqualTo("p1");
    }

    @Test
    void playerDisconnectSendsPlayerIdAndMapsResult() throws Exception {
        this.server.playerResult = false;

        assertThat(await(this.agones.playerDisconnectFuture("p1"))).isFalse();
        assertThat(this.server.takeRequest(Alpha.PlayerID.class).getPlayerID()).isEqualTo("p1");
    }

    @Test
    void isPlayerConnectedMapsResult() throws Exception {
        this.server.playerResult = true;

        assertThat(await(this.agones.isPlayerConnected("p1"))).isTrue();
    }

    @Test
    void getConnectedPlayersMapsList() throws Exception {
        this.server.connectedPlayers = Alpha.PlayerIDList.newBuilder()
            .addAllList(Arrays.asList("p1", "p2"))
            .build();

        assertThat(await(this.agones.getConnectedPlayersFuture())).containsExactly("p1", "p2");
    }

    @Test
    void playerCapacityAndCountMapResponses() throws Exception {
        this.server.playerCount = 16L;

        await(this.agones.setPlayerCapacityFuture(32L));
        assertThat(this.server.takeRequest(Alpha.Count.class).getCount()).isEqualTo(32L);
        assertThat(await(this.agones.getPlayerCapacityFuture())).isEqualTo(16L);
        assertThat(await(this.agones.getPlayerCountFuture())).isEqualTo(16L);
    }

    @Test
    void gameServerWatchersReceiveUpdates() throws Exception {
        this.server.watchUpdates.add(gameServer("gs-1"));
        this.server.watchUpdates.add(gameServer("gs-2"));
        final BlockingQueue<Sdk.GameServer> first = new LinkedBlockingQueue<>();
        final BlockingQueue<Sdk.GameServer> second = new LinkedBlockingQueue<>();

        this.agones.addGameServerWatcher(first::add);
        this.agones.addGameServerWatcher(second::add);

        assertThat(first.poll(5L, TimeUnit.SECONDS)).isEqualTo(gameServer("gs-1"));
        assertThat(first.poll(5L, TimeUnit.SECONDS)).isEqualTo(gameServer("gs-2"));
        assertThat(this.server.takeRequest(Sdk.Empty.class)).isNotNull();
    }

    @Test
    void addingWatchersOpensSingleStream() throws Exception {
        this.agones.addGameServerWatcher(gameServer -> {});
        this.agones.addGameServerWatcher(gameServer -> {});

        this.server.takeRequest(Sdk.Empty.class);
        assertThat(this.server.pollRequest(200L, TimeUnit.MILLISECONDS)).isNull();
    }

    @Test
    void healthCheckingSendsPeriodicPings() throws Exception {
        assertThat(this.agones.canHealthCheck()).isTrue();

        this.agones.startHealthChecking();

        assertThat(this.server.healthPings.tryAcquire(3, 5L, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    void stopHealthCheckingStopsPings() throws Exception {
        this.agones.startHealthChecking();
        assertThat(this.server.healthPings.tryAcquire(1, 5L, TimeUnit.SECONDS)).isTrue();

        this.agones.stopHealthChecking();
        Thread.sleep(50L);
        this.server.healthPings.drainPermits();

        assertThat(this.server.healthPings.tryAcquire(1, 100L, TimeUnit.MILLISECONDS)).isFalse();
    }

    @Test
    void healthCheckAndWatcherAreDisabledByDefault() throws Exception {
        try (Agones bare = Agones.builder().withChannel(this.server.newChannel()).build()) {
            assertThat(bare.canHealthCheck()).isFalse();
            assertThat(bare.canWatchGameServer()).isFalse();
            assertThatThrownBy(bare::startHealthChecking).isInstanceOf(IllegalStateException.class);
            assertThatThrownBy(() -> bare.addGameServerWatcher(gameServer -> {})).isInstanceOf(
                NullPointerException.class
            );
        }
    }

    @Test
    void healthCheckCreatesDefaultExecutorWhenOnlyDurationsAreSet() throws Exception {
        try (
            Agones agones = Agones.builder()
                .withChannel(this.server.newChannel())
                .withHealthCheck(Duration.ZERO, Duration.ofSeconds(1L))
                .build()
        ) {
            assertThat(agones.canHealthCheck()).isTrue();
        }
    }

    private Beta.CounterUpdateRequest takeCounterUpdate() throws InterruptedException {
        return this.server.takeRequest(Beta.UpdateCounterRequest.class).getCounterUpdateRequest();
    }

    private static <T> T await(final CompletableFuture<T> future) throws Exception {
        return future.get(5L, TimeUnit.SECONDS);
    }

    private static Sdk.GameServer gameServer(final String name) {
        return Sdk.GameServer.newBuilder()
            .setObjectMeta(Sdk.GameServer.ObjectMeta.newBuilder().setName(name))
            .build();
    }

    private static Beta.Counter counter(final String name, final long capacity, final long count) {
        return Beta.Counter.newBuilder()
            .setName(name)
            .setCapacity(capacity)
            .setCount(count)
            .build();
    }

    private static Beta.List list(final String name, final long capacity, final String... values) {
        return Beta.List.newBuilder()
            .setName(name)
            .setCapacity(capacity)
            .addAllValues(Arrays.asList(values))
            .build();
    }
}
