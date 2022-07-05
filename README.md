# agones4j
[![idea](https://www.elegantobjects.org/intellij-idea.svg)](https://www.jetbrains.com/idea/)

![Sonatype Nexus (Releases)](https://img.shields.io/nexus/r/tr.com.infumia/agones4j?label=maven-central&server=https%3A%2F%2Foss.sonatype.org%2F)
![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/tr.com.infumia/agones4j?label=maven-central&server=https%3A%2F%2Foss.sonatype.org)
## How to Use (Developers)
### Code
```java
final class Test {

  public static void main(
    final String[] args
  ) {
    final var sdk = new AgonesSdk();
    
  }
}
```
### Maven
```xml
<dependencies>
  <dependency>
    <groupId>tr.com.infumia</groupId>
    <artifactId>agones4j</artifactId>
    <version>VERSION</version>
  </dependency>
</dependencies>
```
### Gradle
```groovy
plugins {
  id "java"
}

dependencies {
  implementation "tr.com.infumia:agones4j:VERSION"
}
```
