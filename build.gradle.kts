import net.infumia.gradle.protobuf
import net.infumia.gradle.publish
import net.infumia.gradle.spotless

plugins { java }

publish()

protobuf()

spotless()

dependencies {
    compileOnly(libs.protobuf)
    compileOnly(libs.grpc.protobuf)
    compileOnly(libs.grpc.stub)
    compileOnly(libs.annotationsapi)
}
