plugins { `kotlin-dsl` }

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // TODO: portlek, remove once
    // https://github.com/gradle/gradle/issues/15383#issuecomment-779893192 is fixed
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

    implementation(libs.nexus.plugin)
    implementation(libs.spotless.plugin)
    implementation(libs.protobuf.plugin)
}

kotlin { jvmToolchain(11) }
