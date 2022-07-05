package tr.com.infumia.agones4j;

/**
 * an interface that contains environment variables.
 */
public interface Vars {
  /**
   * the agones sdk grpc port.
   */
  int AGONES_SDK_GRPC_PORT = Envs.getInt("AGONES_SDK_GRPC_PORT", 9357);

  /**
   * the agones sdk http port.
   */
  int AGONES_SDK_HTTP_PORT = Envs.getInt("AGONES_SDK_HTTP_PORT", 9358);
}
