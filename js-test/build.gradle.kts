/*
Copyright (c) 2018 Codice Foundation

Released under The MIT License; see
http://www.opensource.org/licenses/mit-license.php
or http://en.wikipedia.org/wiki/MIT_License
*/
// Build file
plugins {
    id("com.moowork.grunt").version(Versions.mooworkGrunt)
    id("com.moowork.node").version(Versions.mooworkNode)
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
