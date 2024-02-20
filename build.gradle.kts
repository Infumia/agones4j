import com.google.protobuf.gradle.id

plugins {
  java
  `java-library`
  alias(libs.plugins.protobuf)
}

repositories {
  mavenCentral()
}

dependencies {
  compileOnlyApi(libs.protobuf)
  compileOnlyApi(libs.grpc.protobuf)
  compileOnlyApi(libs.grpc.stub)
  compileOnlyApi(libs.annotationsapi)
}

sourceSets {
  main {
    java {
      srcDirs("build/generated/source/proto/main/java")
    }
    resources {
      srcDir("src/main/proto")
    }
  }
}

protobuf {
  protoc { artifact = libs.protoc.get().toString() }

  plugins {
    id("grpc") {
      artifact = "io.grpc:protoc-gen-grpc-java:${libs.versions.grpc.get()}"
    }
  }

  generateProtoTasks {
    all().forEach {
      it.plugins {
        id("grpc") {
          outputSubDir = "java"
        }
      }
    }
  }
}

tasks {
  compileJava {
    options.encoding = Charsets.UTF_8.name()
  }

  javadoc {
    options.encoding = Charsets.UTF_8.name()
    (options as StandardJavadocDocletOptions).tags("todo")
    exclude("**/allocation/**")
    exclude("**/agones/**")
    exclude("**/com/google/api/**")
    exclude("**/grpc/**")
  }

  val javadocJar by creating(Jar::class) {
    dependsOn("javadoc")
    archiveClassifier.set("javadoc")
    from(javadoc)
  }

  val sourcesJar by creating(Jar::class) {
    dependsOn("classes")
    archiveClassifier.set("sources")
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    from(sourceSets["main"].allSource)
  }

  processResources {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
  }
}
