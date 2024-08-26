package net.infumia.gradle

import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.*
import org.gradle.language.jvm.tasks.ProcessResources

fun Project.applyJava(javaVersion: Int = 8) {
    apply<JavaPlugin>()

    repositories.mavenCentral()

    extensions.configure<JavaPluginExtension> {
        toolchain { languageVersion = JavaLanguageVersion.of(javaVersion) }
    }

    tasks { withType<ProcessResources> { duplicatesStrategy = DuplicatesStrategy.EXCLUDE } }
}
