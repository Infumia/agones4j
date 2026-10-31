import net.infumia.gradle.applyProtobuf
import net.infumia.gradle.applyPublish
import net.infumia.gradle.applySpotless

plugins { java }

applyPublish()

applyProtobuf()

applySpotless()

dependencies {
    compileOnly(libs.protobuf)
    compileOnly(libs.grpc.protobuf)
    compileOnly(libs.grpc.stub)
    compileOnly(libs.annotationsapi)

    testImplementation(libs.protobuf)
    testImplementation(libs.grpc.protobuf)
    testImplementation(libs.grpc.stub)
    testImplementation(libs.grpc.inprocess)
    testImplementation(libs.grpc.netty.shaded)
    testImplementation(libs.annotationsapi)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test { useJUnitPlatform() }
