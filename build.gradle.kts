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
}
