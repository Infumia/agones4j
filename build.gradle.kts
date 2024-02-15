import com.diffplug.spotless.LineEnding
import com.google.protobuf.gradle.id

plugins {
  java
  `java-library`
  `maven-publish`
  signing
  alias(libs.plugins.protobuf)
  alias(libs.plugins.spotless)
  alias(libs.plugins.nexus)
}

val signRequired = !rootProject.property("dev").toString().toBoolean()

group = "tr.com.infumia"

repositories {
  mavenCentral()
}

dependencies {
  compileOnlyApi(libs.protobuf)
  compileOnlyApi(libs.grpc.protobuf)
  compileOnlyApi(libs.grpc.stub)
  compileOnlyApi(libs.annotationsapi)
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
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
    exclude("allocation/**")
    exclude("agones/**")
    exclude("com/google/api/**")
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

spotless {
  lineEndings = LineEnding.UNIX

  java {
    importOrder()
    removeUnusedImports()
    endWithNewline()
    indentWithSpaces(2)
    trimTrailingWhitespace()
    prettier(
      mapOf(
        "prettier" to "3.2.5",
        "prettier-plugin-java" to "2.5.0"
      )
    ).config(
      mapOf(
        "parser" to "java",
        "tabWidth" to 2,
        "useTabs" to false,
        "printWidth" to 120,
        "plugins" to listOf("prettier-plugin-java"),
      )
    )
  }
}

publishing {
  publications {
    val publication = create<MavenPublication>("mavenJava") {
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

    signing {
      isRequired = signRequired
      if (isRequired) {
        useGpgCmd()
        sign(publication)
      }
    }
  }
}

nexusPublishing {
  repositories {
    sonatype()
  }
}
