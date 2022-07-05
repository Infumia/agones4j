package tr.com.infumia.agones4j;

import agones.dev.sdk.alpha.SDKGrpc;
import io.grpc.ManagedChannel;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents Agones Sdk.
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class AgonesAlphaSdk {

  /**
   * the stub.
   */
  @NotNull
  SDKGrpc.SDKStub stub;

  /**
   * ctor.
   *
   * @param channel the channel.
   */
  AgonesAlphaSdk(@NotNull final ManagedChannel channel) {
    this.stub = SDKGrpc.newStub(channel);
  }
}
