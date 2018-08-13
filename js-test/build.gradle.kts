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

node {
    download = true
}

tasks {
    "installGrunt" {
        dependsOn(":usng2-js:npmPrePublish", "npmInstall")
    }

    "grunt_mochaTest" {
        dependsOn("installGrunt")
    }

    "check" {
        dependsOn("grunt_mochaTest")
    }
}
