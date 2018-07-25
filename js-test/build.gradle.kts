plugins {
    id("com.moowork.grunt") version "1.2.0"
    id("com.moowork.node") version "1.2.0"
}

tasks {
    "installGrunt" {
        dependsOn(":usng2-js:build", "npmInstall")
    }

    "grunt_mochaTest" {
        dependsOn("installGrunt")
    }

    "build" {
        dependsOn("grunt_mochaTest")
    }
}
