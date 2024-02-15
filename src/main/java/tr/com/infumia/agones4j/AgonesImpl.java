package tr.com.infumia.agones4j;

import agones.dev.sdk.Sdk;
import agones.dev.sdk.alpha.Alpha;
import com.google.protobuf.FieldMask;
import com.google.protobuf.Int64Value;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

final class AgonesImpl implements Agones {

  private final ManagedChannel channel;

  private final agones.dev.sdk.SDKGrpc.SDKStub sdk;
  private final agones.dev.sdk.beta.SDKGrpc.SDKStub beta;
  private final agones.dev.sdk.alpha.SDKGrpc.SDKStub alpha;

  private final ExecutorService gameServerWatcherExecutor;
  private List<Consumer<Sdk.GameServer>> gameServerWatchers;

  private final Duration healthCheckDelay;
  private final ScheduledExecutorService healthCheckExecutor;
  private final Duration healthCheckPeriod;
  private ScheduledFuture<?> healthCheckTask;

  private AgonesImpl(final Builder builder) {
    this.channel = builder.channel;
    this.gameServerWatcherExecutor = builder.gameServerWatcherExecutor;
    this.healthCheckExecutor = builder.healthCheckExecutor();
    this.healthCheckDelay = builder.healthCheckDelay;
    this.healthCheckPeriod = builder.healthCheckPeriod;
    this.sdk = agones.dev.sdk.SDKGrpc.newStub(builder.channel);
    this.beta = agones.dev.sdk.beta.SDKGrpc.newStub(builder.channel);
    this.alpha = agones.dev.sdk.alpha.SDKGrpc.newStub(builder.channel);
  }

  @Override
  public boolean canWatchGameServer() {
    return this.gameServerWatcherExecutor != null;
  }

  @Override
  public void addGameServerWatcher(final Consumer<Sdk.GameServer> watcher) {
    Objects.requireNonNull(this.gameServerWatcherExecutor, "Game server watcher is not enabled!");
    if (this.gameServerWatchers == null) {
      this.gameServerWatchers = Collections.synchronizedList(new ArrayList<>());
      final StreamObserver<Sdk.GameServer> response = StreamObservers.onNext(
        this.gameServerWatcherExecutor,
        this.gameServerWatchers
      );
      this.sdk.watchGameServer(Sdk.Empty.getDefaultInstance(), response);
    }
    this.gameServerWatchers.add(watcher);
  }

  @Override
  public boolean canHealthCheck() {
    return this.healthCheckExecutor != null && this.healthCheckDelay != null && this.healthCheckPeriod != null;
  }

  @Override
  public void startHealthChecking() {
    if (this.healthCheckExecutor == null || this.healthCheckDelay == null || this.healthCheckPeriod == null) {
      throw new IllegalStateException("Health check is not enabled!");
    }
    if (this.healthCheckTask != null) {
      this.healthCheckTask.cancel(true);
    }
    final StreamObserver<Sdk.Empty> request = this.healthCheckStream(StreamObservers.empty());
    this.healthCheckTask =
      this.healthCheckExecutor.scheduleAtFixedRate(
          () -> {
            if (this.healthCheckTask != null && !this.healthCheckTask.isDone()) {
              request.onNext(Sdk.Empty.getDefaultInstance());
            }
          },
          this.healthCheckDelay.toMillis(),
          this.healthCheckPeriod.toMillis(),
          TimeUnit.MILLISECONDS
        );
  }

  @Override
  public void stopHealthChecking() {
    if (this.healthCheckTask != null) {
      this.healthCheckTask.cancel(true);
      this.healthCheckTask = null;
    }
  }

  @Override
  public StreamObserver<Sdk.Empty> healthCheckStream(final StreamObserver<Sdk.Empty> response) {
    return this.sdk.health(response);
  }

  @Override
  public void allocate(final StreamObserver<Sdk.Empty> response) {
    this.sdk.allocate(Sdk.Empty.getDefaultInstance(), response);
  }

  @Override
  public void getGameServer(final StreamObserver<Sdk.GameServer> response) {
    this.sdk.getGameServer(Sdk.Empty.getDefaultInstance(), response);
  }

  @Override
  public void ready(final StreamObserver<Sdk.Empty> response) {
    this.sdk.ready(Sdk.Empty.getDefaultInstance(), response);
  }

  @Override
  public void reserve(final Duration duration, final StreamObserver<Sdk.Empty> response) {
    this.sdk.reserve(Sdk.Duration.newBuilder().setSeconds(duration.getSeconds()).build(), response);
  }

  @Override
  public void shutdown(final StreamObserver<Sdk.Empty> response) {
    this.sdk.shutdown(Sdk.Empty.getDefaultInstance(), response);
  }

  @Override
  public void setAnnotation(final String key, final String value, final StreamObserver<Sdk.Empty> observer) {
    this.sdk.setAnnotation(Sdk.KeyValue.newBuilder().setKey(key).setValue(value).build(), observer);
  }

  @Override
  public void setLabel(final String key, final String value, final StreamObserver<Sdk.Empty> response) {
    this.sdk.setLabel(Sdk.KeyValue.newBuilder().setKey(key).setValue(value).build(), response);
  }

  @Override
  public void getConnectedPlayersFuture(final StreamObserver<List<String>> response) {
    this.alpha.getConnectedPlayers(
      Alpha.Empty.getDefaultInstance(),
      StreamObservers.map(response, Alpha.PlayerIDList::getListList)
    );
  }

  @Override
  public void playerConnect(final String playerId, final StreamObserver<Boolean> response) {
    this.alpha.playerConnect(
      Alpha.PlayerID.newBuilder().setPlayerID(playerId).build(),
      StreamObservers.map(response, Alpha.Bool::getBool)
    );
  }

  @Override
  public void playerDisconnect(final String playerId, final StreamObserver<Boolean> response) {
    this.alpha.playerDisconnect(
      Alpha.PlayerID.newBuilder().setPlayerID(playerId).build(),
      StreamObservers.map(response, Alpha.Bool::getBool)
    );
  }

  @Override
  public void isPlayerConnected(final String playerId, final StreamObserver<Boolean> response) {
    this.alpha.isPlayerConnected(
      Alpha.PlayerID.newBuilder().setPlayerID(playerId).build(),
      StreamObservers.map(response, Alpha.Bool::getBool)
    );
  }

  @Override
  public void setPlayerCapacity(final long capacity, final StreamObserver<Alpha.Empty> response) {
    this.alpha.setPlayerCapacity(
      Alpha.Count.newBuilder().setCount(capacity).build(),
      StreamObservers.emptyAlpha()
    );
  }

  @Override
  public void getPlayerCapacity(final StreamObserver<Long> response) {
    this.alpha.getPlayerCapacity(Alpha.Empty.getDefaultInstance(), StreamObservers.map(response, Alpha.Count::getCount));
  }

  @Override
  public void getPlayerCount(final StreamObserver<Long> response) {
    this.alpha.getPlayerCount(Alpha.Empty.getDefaultInstance(), StreamObservers.map(response, Alpha.Count::getCount));
  }

  @Override
  public void getList(final String name, final StreamObserver<AgonesList> response) {
    final Alpha.GetListRequest request = Alpha.GetListRequest.newBuilder()
      .setName(name)
      .build();
    this.alpha.getList(request, StreamObservers.map(response, AgonesList::fromAgones));
  }

  @Override
  public void addList(final String name, final String value, final StreamObserver<AgonesList> response) {
    final Alpha.AddListValueRequest request = Alpha.AddListValueRequest.newBuilder()
      .setName(name)
      .setValue(value)
      .build();
    this.alpha.addListValue(request, StreamObservers.map(response, AgonesList::fromAgones));
  }

  @Override
  public void removeList(final String name, final String value, final StreamObserver<AgonesList> response) {
    final Alpha.RemoveListValueRequest request = Alpha.RemoveListValueRequest.newBuilder()
      .setName(name)
      .setValue(value)
      .build();
    this.alpha.removeListValue(request, StreamObservers.map(response, AgonesList::fromAgones));
  }

  @Override
  public void updateList(final AgonesList list, final List<String> updateMask, final StreamObserver<AgonesList> response) {
    final FieldMask mask = FieldMask.newBuilder().addAllPaths(updateMask).build();
    final Alpha.UpdateListRequest request = Alpha.UpdateListRequest.newBuilder()
      .setList(list.toAgones())
      .setUpdateMask(mask)
      .build();
    this.alpha.updateList(request, StreamObservers.map(response, AgonesList::fromAgones));
  }

  @Override
  public void getCounter(final String name, final StreamObserver<AgonesCounter> response) {
    final Alpha.GetCounterRequest request = Alpha.GetCounterRequest.newBuilder()
      .setName(name)
      .build();
    this.alpha.getCounter(request, StreamObservers.map(response, AgonesCounter::fromAgones));
  }

  @Override
  public void increaseCounter(final String name, final long amount, final StreamObserver<AgonesCounter> response) {
    final Alpha.CounterUpdateRequest update = Alpha.CounterUpdateRequest.newBuilder()
      .setName(name)
      .setCountDiff(amount >= 0 ? amount : Math.abs(amount))
      .build();
    final Alpha.UpdateCounterRequest request = Alpha.UpdateCounterRequest.newBuilder()
      .setCounterUpdateRequest(update)
      .build();
    this.alpha.updateCounter(request, StreamObservers.map(response, AgonesCounter::fromAgones));
  }

  @Override
  public void decreaseCounter(final String name, final long amount, final StreamObserver<AgonesCounter> response) {
    final Alpha.CounterUpdateRequest update = Alpha.CounterUpdateRequest.newBuilder()
      .setName(name)
      .setCountDiff(amount >= 0 ? -amount : amount)
      .build();
    final Alpha.UpdateCounterRequest request = Alpha.UpdateCounterRequest.newBuilder()
      .setCounterUpdateRequest(update)
      .build();
    this.alpha.updateCounter(request, StreamObservers.map(response, AgonesCounter::fromAgones));
  }

  @Override
  public void setCounterCount(final String name, final long amount, final StreamObserver<AgonesCounter> response) {
    final Alpha.CounterUpdateRequest update = Alpha.CounterUpdateRequest.newBuilder()
      .setName(name)
      .setCount(Int64Value.newBuilder().setValue(amount).build())
      .build();
    final Alpha.UpdateCounterRequest request = Alpha.UpdateCounterRequest.newBuilder()
      .setCounterUpdateRequest(update)
      .build();
    this.alpha.updateCounter(request, StreamObservers.map(response, AgonesCounter::fromAgones));
  }

  @Override
  public void setCounterCapacity(final String name, final long amount, final StreamObserver<AgonesCounter> response) {
    final Alpha.CounterUpdateRequest update = Alpha.CounterUpdateRequest.newBuilder()
      .setName(name)
      .setCapacity(Int64Value.newBuilder().setValue(amount).build())
      .build();
    final Alpha.UpdateCounterRequest request = Alpha.UpdateCounterRequest.newBuilder()
      .setCounterUpdateRequest(update)
      .build();
    this.alpha.updateCounter(request, StreamObservers.map(response, AgonesCounter::fromAgones));
  }

  @Override
  public void close() throws Exception {
    this.channel.shutdown().awaitTermination(5L, TimeUnit.SECONDS);
  }

  static final class Builder implements Agones.Builder {

    private final ManagedChannel channel;

    private final ExecutorService gameServerWatcherExecutor;

    private final Duration healthCheckDelay;

    private final ScheduledExecutorService healthCheckExecutor;

    private final Duration healthCheckPeriod;

    private Builder(
      final ManagedChannel channel,
      final ExecutorService gameServerWatcherExecutor,
      final ScheduledExecutorService healthCheckExecutor,
      final Duration healthCheckDelay,
      final Duration healthCheckPeriod
    ) {
      this.channel = channel;
      this.gameServerWatcherExecutor = gameServerWatcherExecutor;
      this.healthCheckExecutor = healthCheckExecutor;
      this.healthCheckDelay = healthCheckDelay;
      this.healthCheckPeriod = healthCheckPeriod;
    }

    Builder() {
      this(null, null, null, null, null);
    }

    @Override
    public Agones build() {
      if (this.channel == null) {
        return this.withChannel().build();
      }
      return new AgonesImpl(this);
    }

    @Override
    public Agones.Builder withChannel(final ManagedChannel channel) {
      return new Builder(
        channel,
        this.gameServerWatcherExecutor,
        this.healthCheckExecutor,
        this.healthCheckDelay,
        this.healthCheckPeriod
      );
    }

    @Override
    public Agones.Builder withGameServerWatcherExecutor(final ExecutorService executor) {
      return new Builder(
        this.channel,
        executor,
        this.healthCheckExecutor,
        this.healthCheckDelay,
        this.healthCheckPeriod
      );
    }

    @Override
    public Agones.Builder withHealthCheck(final Duration delay, final Duration period) {
      return new Builder(this.channel, this.gameServerWatcherExecutor, this.healthCheckExecutor, delay, period);
    }

    @Override
    public Agones.Builder withHealthCheckExecutor(final ScheduledExecutorService executor) {
      return new Builder(
        this.channel,
        this.gameServerWatcherExecutor,
        executor,
        this.healthCheckDelay,
        this.healthCheckPeriod
      );
    }

    private ScheduledExecutorService healthCheckExecutor() {
      if (this.healthCheckExecutor != null) {
        return this.healthCheckExecutor;
      }
      if (this.healthCheckDelay != null) {
        return Executors.newSingleThreadScheduledExecutor();
      }
      return null;
    }
  }
}
