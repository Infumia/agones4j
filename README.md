# agones4j
[![idea](https://www.elegantobjects.org/intellij-idea.svg)](https://www.jetbrains.com/idea/)

![Sonatype Nexus (Releases)](https://img.shields.io/nexus/r/tr.com.infumia/agones4j?label=maven-central&server=https%3A%2F%2Foss.sonatype.org%2F)
![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/tr.com.infumia/agones4j?label=maven-central&server=https%3A%2F%2Foss.sonatype.org)
## How to Use (Developers)
### Code
```java
final class Server {

  public static void main(
    final String[] args
  ) {
    final var sdk = new tr.com.infumia.agones4j.AgonesSdk();
  }
}
```
```groovy
dependencies {
  implementation "io.grpc:grpc-stub:1.47.0"
  implementation "io.grpc:grpc-protobuf:1.47.0"
  implementation "io.grpc:grpc-netty:1.47.0"
  implementation "tr.com.infumia:terminable:0.1.1"
  implementation "tr.com.infumia:agones4j:VERSION"
}
```
