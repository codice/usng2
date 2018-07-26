/*
Copyright (c) 2018 Codice Foundation

Released under The MIT License; see
http://www.opensource.org/licenses/mit-license.php
or http://en.wikipedia.org/wiki/MIT_License
*/
// Build file
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import io.gitlab.arturbosch.detekt.extensions.ProfileStorage.defaultProfile

plugins {
    id("net.ltgt.errorprone") version "0.0.15"
    id("com.diffplug.gradle.spotless") version "3.13.0"
    id("io.gitlab.arturbosch.detekt") version "1.0.0.RC7-3"
}

val projectVersion by extra { "1.0.0-SNAPSHOT" }
val detektVersion by extra { "1.0.0.RC7-3" }

allprojects {
    version = projectVersion

    apply(plugin = "java")
    apply(plugin = "com.diffplug.gradle.spotless")
    apply(plugin = "net.ltgt.errorprone")

    repositories {
        jcenter()
        mavenLocal()
        mavenCentral()
    }

    spotless {
        java {
            licenseHeaderFile(rootProject.file("codice.license.kt"))
            trimTrailingWhitespace()
            googleJavaFormat()
        }
        kotlin {
            ktlint()
            if (project.name == "usng2-common") {
                licenseHeaderFile(rootProject.file("progenitor.license.kt"))
            } else {
                licenseHeaderFile(rootProject.file("codice.license.kt"), "(package|// Default package)")
            }
            trimTrailingWhitespace()
            endWithNewline()
        }
        kotlinGradle {
            ktlint()
            licenseHeaderFile(rootProject.file("codice.license.kt"), "// Build file")
            trimTrailingWhitespace()
            endWithNewline()
        }
    }
}

tasks {
    "build" {
        dependsOn("detektCheck")
    }
}

configure<DetektExtension> {
    defaultProfile(Action {
        input = rootProject.projectDir.absolutePath
        config = "$projectDir/detekt.yml"
        filters = ".*/resources/.*,.*/build/.*,.*/src/test/.*"
    })
}
