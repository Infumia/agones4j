package net.infumia.agones4j;

import agones.dev.sdk.Sdk;
import agones.dev.sdk.alpha.Alpha;
import agones.dev.sdk.beta.Beta;
import com.google.protobuf.Message;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * An in-memory Agones SDK server that records every request and replies with configurable values.
 */
final class FakeAgonesServer implements AutoCloseable {

    private final BlockingQueue<Message> requests = new LinkedBlockingQueue<>();
    private final String name = InProcessServerBuilder.generateName();
    private final Server server;

    final Semaphore healthPings = new Semaphore(0);
    final List<Sdk.GameServer> watchUpdates = new CopyOnWriteArrayList<>();

    volatile Status failure;
    volatile Sdk.GameServer gameServer = Sdk.GameServer.getDefaultInstance();
    volatile Beta.Counter counter = Beta.Counter.getDefaultInstance();
    volatile Beta.List list = Beta.List.getDefaultInstance();
    volatile Alpha.PlayerIDList connectedPlayers = Alpha.PlayerIDList.getDefaultInstance();
    volatile boolean playerResult;
    volatile long playerCount;

    FakeAgonesServer() throws IOException {
        this.server = InProcessServerBuilder.forName(this.name)
            .directExecutor()
            .addService(new SdkService())
            .addService(new BetaService())
            .addService(new AlphaService())
            .build()
            .start();
    }

    ManagedChannel newChannel() {
        return InProcessChannelBuilder.forName(this.name).directExecutor().build();
    }

    Message pollRequest(final long timeout, final TimeUnit unit) throws InterruptedException {
        return this.requests.poll(timeout, unit);
    }

    <T extends Message> T takeRequest(final Class<T> type) throws InterruptedException {
        final Message request = this.pollRequest(5L, TimeUnit.SECONDS);
        if (request == null) {
            throw new AssertionError("No request received, expected " + type.getSimpleName());
        }
        return type.cast(request);
    }

    @Override
    public void close() throws InterruptedException {
        this.server.shutdownNow().awaitTermination(5L, TimeUnit.SECONDS);
    }

    private <T> void reply(final Message request, final StreamObserver<T> response, final T value) {
        this.requests.add(request);
        final Status failure = this.failure;
        if (failure != null) {
            response.onError(failure.asRuntimeException());
            return;
        }
        response.onNext(value);
        response.onCompleted();
    }

    private final class SdkService extends agones.dev.sdk.SDKGrpc.SDKImplBase {

        @Override
        public void ready(final Sdk.Empty request, final StreamObserver<Sdk.Empty> response) {
            FakeAgonesServer.this.reply(request, response, Sdk.Empty.getDefaultInstance());
        }

        @Override
        public void allocate(final Sdk.Empty request, final StreamObserver<Sdk.Empty> response) {
            FakeAgonesServer.this.reply(request, response, Sdk.Empty.getDefaultInstance());
        }

        @Override
        public void shutdown(final Sdk.Empty request, final StreamObserver<Sdk.Empty> response) {
            FakeAgonesServer.this.reply(request, response, Sdk.Empty.getDefaultInstance());
        }

        @Override
        public StreamObserver<Sdk.Empty> health(final StreamObserver<Sdk.Empty> response) {
            return new StreamObserver<Sdk.Empty>() {
                @Override
                public void onNext(final Sdk.Empty value) {
                    FakeAgonesServer.this.healthPings.release();
                }

                @Override
                public void onError(final Throwable t) {}

                @Override
                public void onCompleted() {
                    response.onNext(Sdk.Empty.getDefaultInstance());
                    response.onCompleted();
                }
            };
        }

        @Override
        public void getGameServer(
            final Sdk.Empty request,
            final StreamObserver<Sdk.GameServer> response
        ) {
            FakeAgonesServer.this.reply(request, response, FakeAgonesServer.this.gameServer);
        }

        @Override
        public void watchGameServer(
            final Sdk.Empty request,
            final StreamObserver<Sdk.GameServer> response
        ) {
            FakeAgonesServer.this.requests.add(request);
            FakeAgonesServer.this.watchUpdates.forEach(response::onNext);
        }

        @Override
        public void setLabel(final Sdk.KeyValue request, final StreamObserver<Sdk.Empty> response) {
            FakeAgonesServer.this.reply(request, response, Sdk.Empty.getDefaultInstance());
        }

        @Override
        public void setAnnotation(
            final Sdk.KeyValue request,
            final StreamObserver<Sdk.Empty> response
        ) {
            FakeAgonesServer.this.reply(request, response, Sdk.Empty.getDefaultInstance());
        }

        @Override
        public void reserve(final Sdk.Duration request, final StreamObserver<Sdk.Empty> response) {
            FakeAgonesServer.this.reply(request, response, Sdk.Empty.getDefaultInstance());
        }
    }

    private final class BetaService extends agones.dev.sdk.beta.SDKGrpc.SDKImplBase {

        @Override
        public void getCounter(
            final Beta.GetCounterRequest request,
            final StreamObserver<Beta.Counter> response
        ) {
            FakeAgonesServer.this.reply(request, response, FakeAgonesServer.this.counter);
        }

        @Override
        public void updateCounter(
            final Beta.UpdateCounterRequest request,
            final StreamObserver<Beta.Counter> response
        ) {
            FakeAgonesServer.this.reply(request, response, FakeAgonesServer.this.counter);
        }

        @Override
        public void getList(
            final Beta.GetListRequest request,
            final StreamObserver<Beta.List> response
        ) {
            FakeAgonesServer.this.reply(request, response, FakeAgonesServer.this.list);
        }

        @Override
        public void updateList(
            final Beta.UpdateListRequest request,
            final StreamObserver<Beta.List> response
        ) {
            FakeAgonesServer.this.reply(request, response, FakeAgonesServer.this.list);
        }

        @Override
        public void addListValue(
            final Beta.AddListValueRequest request,
            final StreamObserver<Beta.List> response
        ) {
            FakeAgonesServer.this.reply(request, response, FakeAgonesServer.this.list);
        }

        @Override
        public void removeListValue(
            final Beta.RemoveListValueRequest request,
            final StreamObserver<Beta.List> response
        ) {
            FakeAgonesServer.this.reply(request, response, FakeAgonesServer.this.list);
        }
    }

    private final class AlphaService extends agones.dev.sdk.alpha.SDKGrpc.SDKImplBase {

        @Override
        public void playerConnect(
            final Alpha.PlayerID request,
            final StreamObserver<Alpha.Bool> response
        ) {
            FakeAgonesServer.this.reply(request, response, this.playerResult());
        }

        @Override
        public void playerDisconnect(
            final Alpha.PlayerID request,
            final StreamObserver<Alpha.Bool> response
        ) {
            FakeAgonesServer.this.reply(request, response, this.playerResult());
        }

        @Override
        public void isPlayerConnected(
            final Alpha.PlayerID request,
            final StreamObserver<Alpha.Bool> response
        ) {
            FakeAgonesServer.this.reply(request, response, this.playerResult());
        }

        @Override
        public void setPlayerCapacity(
            final Alpha.Count request,
            final StreamObserver<Alpha.Empty> response
        ) {
            FakeAgonesServer.this.reply(request, response, Alpha.Empty.getDefaultInstance());
        }

        @Override
        public void getPlayerCapacity(
            final Alpha.Empty request,
            final StreamObserver<Alpha.Count> response
        ) {
            FakeAgonesServer.this.reply(request, response, this.playerCount());
        }

        @Override
        public void getPlayerCount(
            final Alpha.Empty request,
            final StreamObserver<Alpha.Count> response
        ) {
            FakeAgonesServer.this.reply(request, response, this.playerCount());
        }

        @Override
        public void getConnectedPlayers(
            final Alpha.Empty request,
            final StreamObserver<Alpha.PlayerIDList> response
        ) {
            FakeAgonesServer.this.reply(request, response, FakeAgonesServer.this.connectedPlayers);
        }

        private Alpha.Bool playerResult() {
            return Alpha.Bool.newBuilder().setBool(FakeAgonesServer.this.playerResult).build();
        }

        private Alpha.Count playerCount() {
            return Alpha.Count.newBuilder().setCount(FakeAgonesServer.this.playerCount).build();
        }
    }
}
