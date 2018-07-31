/*
Copyright (c) 2018 Codice Foundation

Released under The MIT License; see
http://www.opensource.org/licenses/mit-license.php
or http://en.wikipedia.org/wiki/MIT_License
*/
// Build file
plugins {
    id("kotlin-platform-common").version(Versions.kotlin)
}

dependencies {
    compile(Libs.kotlinStdlibCommon)
    testCompile(Libs.kotlinTestAnnotationsCommon)
    testCompile(Libs.kotlinTestCommon)
}
