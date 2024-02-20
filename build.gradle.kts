import com.google.protobuf.gradle.id

plugins {
  java
  `java-library`
  `maven-publish`
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

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      groupId = project.group.toString()
      artifactId = "agones4j"
      version = project.version.toString()
      from(components["java"])
      artifact(tasks["sourcesJar"])
      artifact(tasks["javadocJar"])
      pom {
        name.set("Agones4J")
        description.set("Java wrapper for Agones client SDK.")
        url.set("https://infumia.com.tr/")
        licenses {
          license {
            name.set("MIT License")
            url.set("https://mit-license.org/license.txt")
          }
        }
        developers {
          developer {
            id.set("portlek")
            name.set("Hasan Demirtaş")
            email.set("utsukushihito@outlook.com")
          }
        }
        scm {
          connection.set("scm:git:git://github.com/infumia/agones4j.git")
          developerConnection.set("scm:git:ssh://github.com/infumia/agones4j.git")
          url.set("https://github.com/infumia/agones4j")
        }
      }
    }
  }
}
