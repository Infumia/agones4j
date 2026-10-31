package net.infumia.agones4j;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import agones.dev.sdk.Sdk;
import io.grpc.ManagedChannel;
import io.grpc.ServerBuilder;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class AgonesBuilderTest {

    private FakeAgonesServer server;

    @BeforeEach
    void setUp() throws Exception {
        this.server = new FakeAgonesServer(ServerBuilder.forPort(0));
    }

    @AfterEach
    void tearDown() throws Exception {
        this.server.close();
    }

    @Test
    void withAddressHostAndPortConnectsOverTcp() throws Exception {
        try (
            Agones agones = Agones.builder().withAddress("localhost", this.server.port()).build()
        ) {
            this.assertConnected(agones);
        }
    }

    @Test
    void withAddressPortUsesDefaultHost() throws Exception {
        assumeDefaultEnvironment();
        try (Agones agones = Agones.builder().withAddress(this.server.port()).build()) {
            this.assertConnected(agones);
        }
    }

    @Test
    void withTargetConnectsOverTcp() throws Exception {
        try (
            Agones agones = Agones.builder()
                .withTarget("localhost:" + this.server.port())
                .build()
        ) {
            this.assertConnected(agones);
        }
    }

    @Test
    void defaultAddressIsLocalhost9357() throws Exception {
        assumeDefaultEnvironment();
        try (Agones agones = Agones.builder().withAddress().build()) {
            assertThat(channel(agones).authority()).isEqualTo("localhost:9357");
        }
        try (Agones agones = Agones.builder().withAddress("example.com").build()) {
            assertThat(channel(agones).authority()).isEqualTo("example.com:9357");
        }
        try (Agones agones = Agones.builder().withChannel().build()) {
            assertThat(channel(agones).authority()).isEqualTo("localhost:9357");
        }
        try (Agones agones = Agones.builder().build()) {
            assertThat(channel(agones).authority()).isEqualTo("localhost:9357");
        }
    }

    @Test
    void withTargetFailsWithoutAddressEnvironmentVariable() {
        assumeDefaultEnvironment();
        assertThatThrownBy(() -> Agones.builder().withTarget())
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("AGONES_SDK_GRPC_ADDRESS");
    }

    private void assertConnected(final Agones agones) throws Exception {
        agones.readyFuture().get(5L, TimeUnit.SECONDS);
        assertThat(this.server.takeRequest(Sdk.Empty.class)).isNotNull();
    }

    private static void assumeDefaultEnvironment() {
        assumeTrue(System.getenv("AGONES_SDK_GRPC_HOST") == null);
        assumeTrue(System.getenv("AGONES_SDK_GRPC_PORT") == null);
        assumeTrue(System.getenv("AGONES_SDK_GRPC_ADDRESS") == null);
    }

    private static ManagedChannel channel(final Agones agones) throws Exception {
        final Field field = AgonesImpl.class.getDeclaredField("channel");
        field.setAccessible(true);
        return (ManagedChannel) field.get(agones);
    }
}
