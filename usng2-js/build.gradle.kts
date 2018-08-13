/*
Copyright (c) 2018 Codice Foundation

Released under The MIT License; see
http://www.opensource.org/licenses/mit-license.php
or http://en.wikipedia.org/wiki/MIT_License
*/
// Build file
@file:Suppress("StringLiteralDuplication")

import com.beust.klaxon.Klaxon
import com.moowork.gradle.node.npm.NpmTask
import com.moowork.gradle.node.task.NodeTask
import org.jetbrains.kotlin.gradle.frontend.KotlinFrontendExtension
import org.jetbrains.kotlin.gradle.frontend.webpack.WebPackExtension
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

buildscript {
    dependencies {
        classpath(Libs.kotlinGradlePlugin)
        classpath(Libs.mooworkNode)
        classpath(Libs.kotlinFrontendPlugin)
        classpath("com.beust:klaxon:3.0.1")
    }
}

plugins {
    id("kotlin-platform-js").version(Versions.kotlin)
    id("com.moowork.node").version(Versions.mooworkNode)
    id("org.jetbrains.kotlin.frontend").version(Versions.kotlinFrontendPlugin)
}

version = Versions.npmProject

dependencies {
    compile(Libs.kotlinStdlibJs)
    "expectedBy"(project(":usng2-common"))
    testCompile(Libs.kotlinTestJs)
}

configure<KotlinFrontendExtension> {
    downloadNodeJsVersion = "latest"
    sourceMaps = true
    define("PRODUCTION", true)

    bundle("webpack", delegateClosureOf<WebPackExtension> {
        bundleName = "main"
        mode = "production"
    })
}

node {
    download = true
}

tasks {
    "compileKotlin2Js"(Kotlin2JsCompile::class) {
        kotlinOptions {
            metaInfo = true
            outputFile = "${project.buildDir.path}/js/${project.name}.js"
            sourceMap = true
            sourceMapEmbedSources = "always"
            moduleKind = "umd"
        }
    }
    "compileTestKotlin2Js"(Kotlin2JsCompile::class) {
        kotlinOptions {
            moduleKind = "umd"
        }
    }

    "npmPrePublish"(NpmPrePublish::class) {
        dependsOn("gccIt")
    }
    "npmpub"(NpmTask::class) {
        dependsOn("npmPrePublish")
        setWorkingDir(File("$buildDir/npmpub_tmp"))
        setNpmCommand("publish")
        // Setting the args below is necessary if using a scoped package
        // setArgs(listOf("--access", "public"))
    }

    "installBrowserify"(NpmTask::class) {
        dependsOn("npm-index")
        setNpmCommand("install")
        setArgs(listOf("browserify"))
    }
    "installGCC"(NpmTask::class) {
        dependsOn("npm-index")
        setNpmCommand("install")
        setArgs(listOf("google-closure-compiler"))
    }

    "browserifyIt"(NodeTask::class) {
        dependsOn("installBrowserify", "webpack-bundle", "build")
        setScript(file("node_modules/.bin/browserify"))
        setArgs(listOf("$buildDir/js/usng2-js.js",
                "--standalone",
                "usng2",
                "--outfile",
                "$buildDir/bundle/browserified.js"))
    }
    "gccIt"(NodeTask::class) {
        dependsOn("browserifyIt", "installGCC")
        setScript(file("node_modules/.bin/google-closure-compiler"))
        setArgs(listOf("--js",
                "${project.buildDir}/bundle/browserified.js",
                "--js_output_file",
                "${project.buildDir}/bundle/gcc-output.js"))
    }
}

open class NpmPrePublish : DefaultTask() {
    private val outputJsFileName = "usng2.main"

    @TaskAction
    fun run() {
        val outDir = project.mkdir("${project.buildDir}/npmpub_tmp")

        copyMetaFiles(outDir)
        copyJs(outDir)

        editAndCopyPackageJson(outDir)
    }

    fun copyMetaFiles(outDir: File) {
        arrayOf("README", "LICENSE").forEach {
            project.copy {
                from("${project.rootDir}/$it.md") {
                    rename(it, it.toLowerCase())
                }
                into(outDir)
            }
        }
    }

    private fun copyJs(outDir: File) {
        project.copy {
            from("${project.buildDir}/bundle/gcc-output.js") {
                rename("gcc-output", outputJsFileName)
            }
            into(outDir)
        }
    }

    private fun editAndCopyPackageJson(outDir: File) {
        File("${project.buildDir}/package.json").reader().use {
            Klaxon().parseJsonObject(it)
        }.apply {
            remove("dependencies")
            remove("devDependencies")
            File(outDir, "package.json").writeText(this.toJsonString(true))
        }
    }
}
