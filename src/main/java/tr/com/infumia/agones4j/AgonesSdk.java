package tr.com.infumia.agones4j;

import agones.dev.sdk.SDKGrpc;
import agones.dev.sdk.Sdk;
import allocation.Allocation;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import tr.com.infumia.terminable.Terminable;

/**
 * a class that represents Agones Sdk.
 */
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class AgonesSdk implements Terminable {

  /**
   * the alpha.
   */
  @NotNull
  @Getter
  AgonesAlphaSdk alpha;

  /**
   * the beta.
   */
  @NotNull
  @Getter
  AgonesBetaSdk beta;

  /**
   * the channel.
   */
  @NotNull
  ManagedChannel channel;

  /**
   * the stub.
   */
  @NotNull
  SDKGrpc.SDKStub stub;

  /**
   * ctor.
   *
   * @param grpcHost the grpc host.
   * @param grpcPort the grpc port.
   */
  public AgonesSdk(@NotNull final String grpcHost, final int grpcPort) {
    this.channel =
      ManagedChannelBuilder
        .forAddress(grpcHost, grpcPort)
        .usePlaintext()
        .build();
    this.stub = SDKGrpc.newStub(this.channel);
    this.alpha = new AgonesAlphaSdk(this.channel);
    this.beta = new AgonesBetaSdk(this.channel);
  }

  /**
   * ctor.
   *
   * @param grpcHost the grpc host.
   */
  public AgonesSdk(@NotNull final String grpcHost) {
    this(grpcHost, Vars.AGONES_SDK_GRPC_PORT);
  }

  /**
   * ctor.
   *
   * @param grpcPort the grpc port.
   */
  public AgonesSdk(final int grpcPort) {
    this("localhost", grpcPort);
  }

  /**
   * ctor.
   */
  public AgonesSdk() {
    this(Vars.AGONES_SDK_GRPC_PORT);
  }

  /**
   * call to self {@link Allocation} the {@link Sdk.GameServer}.
   *
   * @param observer the observer to call.
   */
  public void allocate(@NotNull final StreamObserver<Sdk.Empty> observer) {
    this.stub.allocate(Sdk.Empty.getDefaultInstance(), observer);
  }

  @Override
  public void close() throws Exception {
    this.channel.shutdown().awaitTermination(5L, TimeUnit.SECONDS);
  }

  /**
   * retrieve the current {@link Sdk.GameServer} data.
   *
   * @param observer the observer to retrieve.
   */
  public void getGameServer(
    @NotNull final StreamObserver<Sdk.GameServer> observer
  ) {
    this.stub.getGameServer(Sdk.Empty.getDefaultInstance(), observer);
  }

  /**
   * send a {@link Sdk.Empty} every duration to declare that this {@link Sdk.GameServer} is healthy.
   *
   * @param observer the observer to send.
   *
   * @return observer.
   */
  @NotNull
  public StreamObserver<Sdk.Empty> health(
    @NotNull final StreamObserver<Sdk.Empty> observer
  ) {
    return this.stub.health(observer);
  }

  /**
   * call when the {@link Sdk.GameServer} is ready.
   *
   * @param observer the observer to call.
   */
  public void ready(@NotNull final StreamObserver<Sdk.Empty> observer) {
    this.stub.ready(Sdk.Empty.getDefaultInstance(), observer);
  }

  /**
   * marks the {@link Sdk.GameServer} as the Reserved state for Duration.
   *
   * @param duration the duration to mark.
   * @param observer the observer to mark.
   */
  public void reserve(
    @NotNull final Sdk.Duration duration,
    @NotNull final StreamObserver<Sdk.Empty> observer
  ) {
    this.stub.reserve(duration, observer);
  }

  /**
   * apply an Annotation to the backing {@link Sdk.GameServer} metadata.
   *
   * @param keyValue the key value to apply.
   * @param observer the observer to apply.
   */
  public void setAnnotation(
    @NotNull final Sdk.KeyValue keyValue,
    @NotNull final StreamObserver<Sdk.Empty> observer
  ) {
    this.stub.setAnnotation(keyValue, observer);
  }

  /**
   * apply an Annotation to the backing {@link Sdk.GameServer} metadata.
   *
   * @param key the key to apply.
   * @param value the value to apply.
   * @param observer the observer to apply.
   *
   * @see #setAnnotation(Sdk.KeyValue, StreamObserver)
   */
  public void setAnnotation(
    @NotNull final String key,
    @NotNull final String value,
    @NotNull final StreamObserver<Sdk.Empty> observer
  ) {
    this.setAnnotation(
        Sdk.KeyValue.newBuilder().setKey(key).setValue(value).build(),
        observer
      );
  }

  /**
   * apply a Label to the backing {@link Sdk.GameServer} metadata.
   *
   * @param keyValue the key value to apply.
   * @param observer the observer to apply.
   */
  public void setLabel(
    @NotNull final Sdk.KeyValue keyValue,
    @NotNull final StreamObserver<Sdk.Empty> observer
  ) {
    this.stub.setLabel(keyValue, observer);
  }

  /**
   * apply a Label to the backing {@link Sdk.GameServer} metadata.
   *
   * @param key the key to apply.
   * @param value the value to apply.
   * @param observer the observer to apply.
   *
   * @see #setLabel(Sdk.KeyValue, StreamObserver)
   */
  public void setLabel(
    @NotNull final String key,
    @NotNull final String value,
    @NotNull final StreamObserver<Sdk.Empty> observer
  ) {
    this.setLabel(
        Sdk.KeyValue.newBuilder().setKey(key).setValue(value).build(),
        observer
      );
  }

  /**
   * call when the {@link Sdk.GameServer} is shutting down.
   *
   * @param observer the observer to call.
   */
  public void shutdown(@NotNull final StreamObserver<Sdk.Empty> observer) {
    this.stub.shutdown(Sdk.Empty.getDefaultInstance(), observer);
  }

  /**
   * send {@link Sdk.GameServer} details whenever the {@link Sdk.GameServer} is updated.
   *
   * @param observer the observer to send.
   */
  public void watchGameServer(
    @NotNull final StreamObserver<Sdk.GameServer> observer
  ) {
    this.stub.watchGameServer(Sdk.Empty.getDefaultInstance(), observer);
  }
}
