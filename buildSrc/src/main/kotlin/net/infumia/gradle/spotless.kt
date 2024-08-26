package net.infumia.gradle

import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessPlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

fun Project.applySpotless() {
    repositories.mavenCentral()

    apply<SpotlessPlugin>()

    extensions.configure<SpotlessExtension> {
        isEnforceCheck = false
        lineEndings = com.diffplug.spotless.LineEnding.UNIX

        val prettierConfig =
            mapOf(
                "prettier" to "3.3.2",
                "prettier-plugin-java" to "2.6.0",
                "prettier-plugin-toml" to "2.0.1",
            )

        yaml {
            target(".github/**/*.yml")
            endWithNewline()
            trimTrailingWhitespace()
            jackson().yamlFeature("LITERAL_BLOCK_STYLE", true).yamlFeature("SPLIT_LINES", false)
        }

        json {
            target("renovate.json")
            endWithNewline()
            trimTrailingWhitespace()
            jackson()
        }

        format("toml") {
            target("gradle/libs.versions.toml")
            endWithNewline()
            trimTrailingWhitespace()
            prettier(prettierConfig)
                .config(
                    mapOf(
                        "parser" to "toml",
                        "plugins" to listOf("prettier-plugin-toml"),
                    ),
                )
        }

        kotlin {
            target(
                "buildSrc/src/main/kotlin/**/*.kt",
                "buildSrc/**/*.gradle.kts",
                "*.gradle.kts",
            )
            endWithNewline()
            trimTrailingWhitespace()
            ktfmt().kotlinlangStyle().configure {
                it.setMaxWidth(100)
                it.setBlockIndent(4)
                it.setContinuationIndent(4)
                it.setRemoveUnusedImport(true)
            }
        }

        java {
            target(
                "src/main/java/**/*.java",
            )
            importOrder()
            removeUnusedImports()
            endWithNewline()
            trimTrailingWhitespace()
            prettier(prettierConfig)
                .config(
                    mapOf(
                        "parser" to "java",
                        "tabWidth" to 4,
                        "useTabs" to false,
                        "printWidth" to 100,
                        "plugins" to listOf("prettier-plugin-java"),
                    ),
                )
        }
    }
}
