import com.diffplug.spotless.LineEnding
import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf

plugins {
  java
  `maven-publish`
  signing
  alias(libs.plugins.protobuf)
  alias(libs.plugins.spotless)
  alias(libs.plugins.shadow)
  alias(libs.plugins.nexus)
}

val signRequired = !rootProject.property("dev").toString().toBoolean()

group = "tr.com.infumia"

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(17))
  }
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
    exclude("**/proto/**")
  }

  val javadocJar by creating(Jar::class) {
    dependsOn("javadoc")
    archiveClassifier.set("javadoc")
    from(javadoc)
  }

  val sourcesJar by creating(Jar::class) {
    dependsOn("classes")
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
  }

  processResources {
    exclude("**/*.proto")
  }

  build {
    dependsOn(spotlessApply)
    dependsOn(jar)
    dependsOn(sourcesJar)
    dependsOn(javadocJar)
  }
}

repositories {
  mavenCentral()
}

dependencies {
  compileOnly(libs.lombok)
  compileOnly(libs.annotations)
  compileOnly(libs.grpc)

  annotationProcessor(libs.lombok)
  annotationProcessor(libs.annotations)

  testAnnotationProcessor(libs.lombok)
  testAnnotationProcessor(libs.annotations)
}

spotless {
  lineEndings = LineEnding.UNIX
  isEnforceCheck = false

  java {
    importOrder()
    removeUnusedImports()
    endWithNewline()
    indentWithSpaces(2)
    trimTrailingWhitespace()
    prettier(
      mapOf(
        "prettier" to "2.7.1",
        "prettier-plugin-java" to "1.6.2"
      )
    ).config(
      mapOf(
        "parser" to "java",
        "tabWidth" to 2,
        "useTabs" to false
      )
    )
  }
}

publishing {
  publications {
    val publication = create<MavenPublication>("mavenJava") {
      groupId = project.group.toString()
      artifactId = "agone4j"
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
