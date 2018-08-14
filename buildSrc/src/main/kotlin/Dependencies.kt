/*
Copyright (c) 2018 Codice Foundation

Released under The MIT License; see
http://www.opensource.org/licenses/mit-license.php
or http://en.wikipedia.org/wiki/MIT_License
*/
//  Default package
@file:Suppress("MaxLineLength")
object Versions {
    const val project = "1.0-SNAPSHOT"
    const val npmProject = "0.9.9-test-a"

    const val javaTarget = "1.8"

    const val kotlin = "1.2.60"
    const val kotlinFrontendPlugin = "0.0.33"
    const val spotless = "3.10.0"
    const val errorprone = "0.0.16"
    const val googleErrorProne = "2.3.1"
    const val testLogger = "1.4.0"
    const val detekt = "1.0.0.RC8"
    const val mooworkGrunt = "1.2.0"
    const val mooworkNode = "1.2.0"
    const val junit = "4.12"
}

object Libs {
    const val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"

    const val kotlinStdlibCommon = "org.jetbrains.kotlin:kotlin-stdlib-common:${Versions.kotlin}"
    const val kotlinStdlibJdk8 = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
    const val kotlinStdlibJs = "org.jetbrains.kotlin:kotlin-stdlib-js:${Versions.kotlin}"

    const val kotlinTestCommon = "org.jetbrains.kotlin:kotlin-test-common:${Versions.kotlin}"
    const val kotlinTestJdk = "org.jetbrains.kotlin:kotlin-test:${Versions.kotlin}"
    const val kotlinTestJs = "org.jetbrains.kotlin:kotlin-test-js:${Versions.kotlin}"

    const val kotlinTestAnnotationsCommon = "org.jetbrains.kotlin:kotlin-test-annotations-common:${Versions.kotlin}"
    const val kotlinTestJunit = "org.jetbrains.kotlin:kotlin-test-junit:${Versions.kotlin}"
    const val kotlinFrontendPlugin = "org.jetbrains.kotlin:kotlin-frontend-plugin:${Versions.kotlinFrontendPlugin}"
    const val mooworkNode = "com.moowork.gradle:gradle-node-plugin:${Versions.mooworkNode}"
    const val junit = "junit:junit:${Versions.junit}"

    const val googleErrorProne = "com.google.errorprone:error_prone_core:${Versions.googleErrorProne}"
}