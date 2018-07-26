/*
Copyright (c) 2018 Codice Foundation

Released under The MIT License; see
http://www.opensource.org/licenses/mit-license.php
or http://en.wikipedia.org/wiki/MIT_License
*/
// Build file
plugins {
    id("kotlin-platform-common") version "1.2.51"
}

dependencies {
    compile("org.jetbrains.kotlin:kotlin-stdlib-common")
    testCompile("org.jetbrains.kotlin:kotlin-test-annotations-common")
    testCompile("org.jetbrains.kotlin:kotlin-test-common")
}
