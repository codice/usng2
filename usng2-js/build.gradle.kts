/*
Copyright (c) 2018 Codice Foundation

Released under The MIT License; see
http://www.opensource.org/licenses/mit-license.php
or http://en.wikipedia.org/wiki/MIT_License
*/
// Build file
import com.moowork.gradle.node.npm.NpmTask
import org.jetbrains.kotlin.gradle.frontend.KotlinFrontendExtension
import org.jetbrains.kotlin.gradle.frontend.webpack.WebPackExtension
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

buildscript {
    repositories {
        jcenter()
        mavenLocal()
        mavenCentral()
        maven { url = uri("https://dl.bintray.com/kotlin/kotlin-eap") }
    }
    dependencies {
        classpath(Libs.kotlinGradlePlugin)
        classpath(Libs.mooworkNode)
        classpath(Libs.kotlinFrontendPlugin)
    }
}

plugins {
    id("kotlin-platform-js").version(Versions.kotlin)
    id("com.moowork.node").version(Versions.mooworkNode)
}

version = Versions.npmProject

apply(plugin = "org.jetbrains.kotlin.frontend")

dependencies {
    compile(Libs.kotlinStdlibJs)
    "expectedBy"(project(":usng2-common"))
    testCompile(Libs.kotlinTestJs)
}

configure<KotlinFrontendExtension> {
    downloadNodeJsVersion = "latest"
    sourceMaps = true

    bundle("webpack", delegateClosureOf<WebPackExtension> {
        bundleName = "main"
        mode = "production"
    })
}

tasks {
    "compileKotlin2Js"(Kotlin2JsCompile::class) {
        kotlinOptions {
            moduleKind = "umd"
        }
    }
    "compileTestKotlin2Js"(Kotlin2JsCompile::class) {
        kotlinOptions {
            moduleKind = "umd"
        }
    }

    "npmPrePublish"(NpmPrePublish::class) {
        dependsOn("build")
    }
    "npmpub"(NpmTask::class) {
        dependsOn("npmPrePublish")
        setWorkingDir(File("$buildDir/npmpub_tmp"))
        setNpmCommand("publish")
        // Setting the args below is necessary if using a scoped package
        setArgs(listOf("--access", "public"))
    }
}

open class NpmPrePublish : DefaultTask() {
    @TaskAction
    fun run() {
        val outDir = project.mkdir("${project.buildDir}/npmpub_tmp")
        project.copy {
            from("${project.rootDir}/README.md") {
                rename("README", "readme")
            }
            into(outDir)
        }
        project.copy {
            from("${project.buildDir}/package.json")
            into(outDir)
        }
        project.copy {
            from("${project.buildDir}/classes/kotlin/main/usng2-js.js")
            into(outDir)
        }
    }
}
