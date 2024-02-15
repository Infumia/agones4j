package tr.com.infumia.agones4j;

/**
 * Utility class that provides default values for environment variables and exposes properties for GRPC and HTTP ports.
 */
final class Vars {

  /**
   * Default GRPC port.
   */
  private static final int DEFAULT_GRPC_PORT = 9357;

  /**
   * Default GRPC host.
   */
  private static final String DEFAULT_GRPC_HOST = "localhost";

  /**
   * The environment variable name for the GRPC host.
   */
  private static final String ENV_GRPC_HOST = "AGONES_SDK_GRPC_HOST";

  /**
   * The environment variable name for the GRPC port.
   */
  private static final String ENV_GRPC_PORT = "AGONES_SDK_GRPC_PORT";

  /**
   * The environment variable name for the GRPC address.
   */
  static final String ENV_GRPC_ADDRESS = "AGONES_SDK_GRPC_ADDRESS";

  /**
   * The GRPC host.
   */
  static final String GRPC_HOST = Envs.get(Vars.ENV_GRPC_HOST, Vars.DEFAULT_GRPC_HOST);

  /**
   * The GRPC port.
   */
  static final int GRPC_PORT = Envs.getInt(Vars.ENV_GRPC_PORT, Vars.DEFAULT_GRPC_PORT);

  /**
   * The GRPC address. Null if not set.
   */
  static final String GRPC_ADDRESS = Envs.get(Vars.ENV_GRPC_ADDRESS, null);

  /**
   * Ctor.
   */
  private Vars() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }
}
