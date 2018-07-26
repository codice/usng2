/*
Copyright (c) 2018 Codice Foundation

Released under The MIT License; see
http://www.opensource.org/licenses/mit-license.php
or http://en.wikipedia.org/wiki/MIT_License
*/
// Build file
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("kotlin-platform-jvm") version "1.2.51"
    id("com.adarshr.test-logger") version "1.3.1"
    `maven-publish`
}

dependencies {
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    "expectedBy"(project(":usng2-common"))
    testCompile("junit:junit:4.12")
    testCompile("org.jetbrains.kotlin:kotlin-test")
    testCompile("org.jetbrains.kotlin:kotlin-test-junit")
}

tasks {
    "compileKotlin"(KotlinCompile::class) {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
    "compileTestKotlin"(KotlinCompile::class) {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
    val sourceCompatibility = "1.8"

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
