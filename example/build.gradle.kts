import java.nio.file.Path

plugins {
    java
    id("io.github.goooler.shadow") version "8.1.7"
}

group = "tr.com.infumia"
version = "1.0.0-SNAPSHOT"

val tiltFolder: Path = rootProject.layout.projectDirectory.asFile.toPath().resolve("tilt")

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks {
    jar {
        archiveVersion = ""
    }

    shadowJar {
        archiveClassifier = ""
        archiveVersion = ""

        mergeServiceFiles()

        manifest {
            attributes(
                "Main-Class" to "tr.com.infumia.Main"
            )
        }
    }

    val outputTask = shadowJar.get()

    register("copyJar") {
        val inputJar = outputTask.outputs.files.singleFile
        val outputFile = tiltFolder
            .resolve("example")
            .resolve("docker")
            .resolve("example.jar")
            .toFile()

        inputs.files(inputJar)
        outputs.files(outputFile)

        doLast {
            inputJar.copyTo(outputFile, true)
        }

        dependsOn(shadowJar)
    }

    build {
        dependsOn(shadowJar)
        finalizedBy("copyJar")
    }
}

repositories {
    mavenCentral()
    maven("https://jitpack.io/")
}

dependencies {
    implementation("io.grpc:grpc-stub:1.63.0")
    implementation("io.grpc:grpc-protobuf:1.63.0")
    implementation("io.grpc:grpc-netty:1.63.0")
    implementation("com.github.infumia:agones4j:1.0.3")
}
