package tr.com.infumia.agones4j;

import agones.dev.sdk.beta.SDKGrpc;
import io.grpc.ManagedChannel;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents Agones Sdk.
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class AgonesBetaSdk {

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
  AgonesBetaSdk(@NotNull final ManagedChannel channel) {
    this.stub = SDKGrpc.newStub(channel);
  }
}
