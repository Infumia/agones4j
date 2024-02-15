# agones4j
![Sonatype Nexus (Releases)](https://img.shields.io/nexus/r/tr.com.infumia/agones4j?label=maven-central&server=https%3A%2F%2Foss.sonatype.org%2F)
![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/tr.com.infumia/agones4j?label=maven-central&server=https%3A%2F%2Foss.sonatype.org)
## How to Use (Developers)
### Code
```groovy
dependencies {
  implementation "io.grpc:grpc-stub:1.47.0"
  implementation "io.grpc:grpc-protobuf:1.47.0"
  implementation "io.grpc:grpc-netty:1.47.0"
  implementation "tr.com.infumia:agones4j:VERSION"
}
```
```java
import java.time.Duration;

void agones() {
  final ExecutorService gameServerWatcherExecutor =
    Executors.newSingleThreadExecutor();
  final ScheduledExecutorService healthCheckExecutor =
    Executors.newSingleThreadScheduledExecutor();
  final Agones agones = Agones.builder()
    // Address specification.
    // If not specified, localhost:9357 will be used.
    // All the following methods are creating a ManagedChannel with 'usePlaintext'
    // If you need to use SSL, you can use 'withChannel(ManagedChannel)' method.
    .withAddress("localhost", 9357)
    .withAddress("localhost") // 9357 
    .withAddress(9357) // localhost
    .withAddress() // localhost 9357
    .withTarget("localhost:9357")
    .withTarget() // localhost:9357
    .withChannel(ManagedChannelBuilder
      .forAddress("localhost", 9357)
      .usePlaintext()
      .build())
    .withChannel() // localhost:9357
    // Game server watcher executor specification.
    .withGameServerWatcherExecutor(gameServerWatcherExecutor)
    // Health checker executor specification.
    // Check you game server's health check threshold and
    // set the executor's delay and period accordingly.
    .withHealthCheck(
      /* delay */Duration.ofSeconds(1L),
      /* period */Duration.ofSeconds(2L)
    )
    .withHealthCheckerExecutor(healthCheckExecutor)
    .build();

  // Health checking.
  // Checks if the executor, delay and period are specified.
  if (agones.canHealthCheck()) {
    // Automatic health checking.
    // Uses the health checker executor and the specified delay and period.
    agones.startHealthChecking();
  }
    
  // Manual health checking.
  final StreamObserver<Empty> requester = agones.healthCheck();
  // onNext needs to be called continuously to keep the game server healthy.
  requester.onNext(Empty.getDefaultInstance());
  
  // Stopping the health checking.
  agones.stopHealthChecking();

  // Game server watching.
  // Checks if the executor is specified.
  if (agones.canWatchGameServer()) {
    agones.addGameServerWatcher(gameServer ->
      // This will be called when the game server is updated.
      System.out.println("Game server updated: " + gameServer));
  }

  agones.allocate();
  agones.shutdown();
}
```
