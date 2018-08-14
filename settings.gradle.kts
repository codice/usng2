/*
Copyright (c) 2018 Codice Foundation

Released under The MIT License; see
http://www.opensource.org/licenses/mit-license.php
or http://en.wikipedia.org/wiki/MIT_License
*/
// Build file
pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "kotlin-platform-common") {
                useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
            }

            if (requested.id.id == "kotlin-platform-jvm") {
                useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
            }

            if (requested.id.id == "kotlin-platform-js") {
                useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
            }
        }
    }
}
rootProject.name = "usng2"
enableFeaturePreview("STABLE_PUBLISHING")

include("usng2-common", "usng2-jvm", "usng2-js", "js-test")
