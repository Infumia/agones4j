package net.infumia.gradle

import com.google.protobuf.gradle.ProtobufExtension
import com.google.protobuf.gradle.ProtobufPlugin
import com.google.protobuf.gradle.id
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.kotlin.dsl.*

fun Project.applyProtobuf() {
    val libs = project.rootProject.the<LibrariesForLibs>()

    apply<ProtobufPlugin>()

    extensions.getByType<JavaPluginExtension>().sourceSets {
        named("main") {
            java { srcDirs("build/generated/source/proto/main/java") }

            resources { srcDir("src/main/proto") }
        }
    }

    extensions.configure<ProtobufExtension> {
        protoc { artifact = libs.protoc.get().toString() }

        plugins {
            id("grpc") { artifact = "io.grpc:protoc-gen-grpc-java:${libs.versions.grpc.get()}" }
        }

        generateProtoTasks { all().forEach { it.plugins { id("grpc") { outputSubDir = "java" } } } }
    }

    tasks {
        withType<Javadoc> {
            exclude("**/allocation/**")
            exclude("**/agones/**")
            exclude("**/com/google/api/**")
            exclude("**/grpc/**")
        }
    }
}
