/*
Copyright (c) 2018 Codice Foundation

Released under The MIT License; see
http://www.opensource.org/licenses/mit-license.php
or http://en.wikipedia.org/wiki/MIT_License
*/
// Build file
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("kotlin-platform-jvm").version(Versions.kotlin)
    id("com.adarshr.test-logger").version(Versions.testLogger)
    `maven-publish`
}

dependencies {
    compile(Libs.kotlinStdlibJdk8)
    "expectedBy"(project(":usng2-common"))
    testCompile(Libs.junit)
    testCompile(Libs.kotlinTestJdk)
    testCompile(Libs.kotlinTestJunit)
}

tasks {
    "compileKotlin"(KotlinCompile::class) {
        kotlinOptions {
            jvmTarget = Versions.javaTarget
        }
    }
    "compileTestKotlin"(KotlinCompile::class) {
        kotlinOptions {
            jvmTarget = Versions.javaTarget
        }
    }
    val sourceCompatibility = Versions.javaTarget

    "build" {
        dependsOn("publishToMavenLocal")
    }
}

publishing {
    (publications) {
        "mavenJava"(MavenPublication::class) {
            from(components["java"])
            pom {
                groupId = "org.codice.usng2"
                artifactId = "usng2"
                name.set("USNG2 Java Library")
                description.set("Java library for GEO conversions between LL, UTM, USNG, and MGRS")
                url.set("https://github.com/codice/usng2")
                licenses {
                    license {
                        name.set("The MIT License")
                        url.set("http://www.opensource.org/licenses/mit-license.php")
                    }
                }
                scm {
                    url.set("https://github.com/codice/usng2")
                    connection.set("scm:git:https://github.com/codice/usng2.git")
                    developerConnection.set("scm:git:git://github.com/codice/usng2.git")
                }
            }
        }
    }

    repositories {
        maven {
            val snapshotsRepoUrl = "\${snapshots.repository.url}"
            val releasesRepoUrl = "\${releases.repository.url}"
            url = if (version.toString().endsWith("SNAPSHOT")) {
                uri(snapshotsRepoUrl)
            } else {
                uri(releasesRepoUrl)
            }
        }
    }
}
