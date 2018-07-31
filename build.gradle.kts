/*
Copyright (c) 2018 Codice Foundation

Released under The MIT License; see
http://www.opensource.org/licenses/mit-license.php
or http://en.wikipedia.org/wiki/MIT_License
*/
// Build file

plugins {
    id("net.ltgt.errorprone").version(Versions.errorprone)
    id("com.diffplug.gradle.spotless").version(Versions.spotless)
    id("io.gitlab.arturbosch.detekt").version(Versions.detekt)
}

allprojects {
    version = Versions.project

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

detekt {
    version = Versions.detekt

    defaultProfile(Action {
        input = rootProject.projectDir.absolutePath
        config = "$projectDir/detekt.yml"
        filters = ".*/resources/.*,.*/build/.*,.*/src/test/.*"
    })
}
