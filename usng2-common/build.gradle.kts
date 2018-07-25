plugins {
    id("kotlin-platform-common") version "1.2.51"
}

dependencies {
    compile("org.jetbrains.kotlin:kotlin-stdlib-common")
    testCompile("org.jetbrains.kotlin:kotlin-test-annotations-common")
    testCompile("org.jetbrains.kotlin:kotlin-test-common")
}
