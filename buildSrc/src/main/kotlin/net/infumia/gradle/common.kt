package net.infumia.gradle

import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.bundling.Jar
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.*
import org.gradle.language.jvm.tasks.ProcessResources

fun Project.applyCommon(javaVersion: Int = 8, sources: Boolean = true, javadoc: Boolean = true) {
    apply<JavaPlugin>()

    repositories.mavenCentral()

    extensions.configure<JavaPluginExtension> {
        toolchain { languageVersion = JavaLanguageVersion.of(javaVersion) }
    }

    if (javadoc) {
        val javadocJar by
            tasks.creating(Jar::class) {
                dependsOn("javadoc")
                archiveClassifier.set("javadoc")
                from(javadoc)
            }
    }

    if (sources) {
        val sourceSets = extensions.getByType<JavaPluginExtension>().sourceSets
        val sourcesJar by
            tasks.creating(Jar::class) {
                dependsOn("classes")
                archiveClassifier.set("sources")
                from(sourceSets["main"].allSource)
                duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            }
    }

    tasks { withType<ProcessResources> { duplicatesStrategy = DuplicatesStrategy.EXCLUDE } }
}
