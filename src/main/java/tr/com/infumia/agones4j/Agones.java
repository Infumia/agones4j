package tr.com.infumia.agones4j;

import agones.dev.sdk.Sdk;
import agones.dev.sdk.alpha.Alpha;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

/**
 * The interface provides methods to interact with Agones.
 */
public interface Agones extends AutoCloseable {
    /**
     * Retrieves a new instance of the Agones builder.
     *
     * @return A new instance of the Agones builder.
     */
    static Agones.Builder builder() {
        return new AgonesImpl.Builder();
    }

    /**
     * Checks if it can watch game server.
     * <p>
     * To enable game server watcher, use {@link Builder#withGameServerWatcherExecutor(ExecutorService)}.
     *
     * @return {@code true} if it can watch game server, {@code false} otherwise.
     */
    boolean canWatchGameServer();

    /**
     * Adds a game server watcher.
     * <p>
     * Game server watchers are called whenever a game server event occurs.
     *
     * @param watcher A consumer that represents the game server watcher.
     *
     * @see #canWatchGameServer()
     */
    void addGameServerWatcher(Consumer<Sdk.GameServer> watcher);

    /**
     * Checks if it can use health check system of Agones.
     * <p>
     * To enable health check, use {@link Builder#withHealthCheck(Duration, Duration)}.
     *
     * @return {@code true} if it can use health check system of Agones, {@code false} otherwise.
     */
    boolean canHealthCheck();

    /**
     * Starts the health checking.
     *
     * @throws IllegalStateException if the health check interval not specified.
     * @see #canHealthCheck()
     */
    void startHealthChecking();

    /**
     * Stops the health checking.
     */
    void stopHealthChecking();

    /**
     * Creates a new health check stream.
     *
     * @param response the response from server.
     *
     * @return a stream observer to send health check requests.
     *
     * @see #canHealthCheck()
     */
    StreamObserver<Sdk.Empty> healthCheckStream(StreamObserver<Sdk.Empty> response);

    /**
     * Creates a new health check stream.
     *
     * @return a stream observer to send health check requests.
     *
     * @see #canHealthCheck()
     */
    default StreamObserver<Sdk.Empty> healthCheckStream() {
        return this.healthCheckStream(Internal.observerEmpty());
    }

    /**
     * Call to self Allocation the GameServer.
     *
     * @param response the response from server.
     */
    void allocate(StreamObserver<Sdk.Empty> response);

    /**
     * Call to self Allocation the GameServer.
     */
    default void allocate() {
        this.allocate(Internal.observerEmpty());
    }

    /**
     * Call to self Allocation the GameServer.
     *
     * @return A future that represents the result of the allocation operation.
     */
    default CompletableFuture<Sdk.Empty> allocateFuture() {
        return Internal.observerToFuture(this::allocate);
    }

    /**
     * Retrieves the current GameServer data.
     *
     * @param response the response from server.
     */
    void getGameServer(StreamObserver<Sdk.GameServer> response);

    /**
     * Retrieves the current GameServer data.
     *
     * @return A future that represents the current game server.
     */
    default CompletableFuture<Sdk.GameServer> getGameServerFuture() {
        return Internal.observerToFuture(this::getGameServer);
    }

    /**
     * Call when the GameServer is ready.
     *
     * @param response the response from server.
     */
    void ready(StreamObserver<Sdk.Empty> response);

    /**
     * Call when the GameServer is ready.
     */
    default void ready() {
        this.ready(Internal.observerEmpty());
    }

    /**
     * Call when the @link Sdk.GameServer is ready.
     *
     * @return A future that represents the result of the ready operation.
     */
    default CompletableFuture<Sdk.Empty> readyFuture() {
        return Internal.observerToFuture(this::ready);
    }

    /**
     * Marks the @link Sdk.GameServer as the Reserved state for Duration.
     *
     * @param duration the duration to mark.
     * @param response the response from server.
     */
    void reserve(Duration duration, StreamObserver<Sdk.Empty> response);

    /**
     * Marks the @link Sdk.GameServer as the Reserved state for Duration.
     *
     * @param duration the duration to mark.
     */
    default void reserve(final Duration duration) {
        this.reserve(duration, Internal.observerEmpty());
    }

    /**
     * Marks the @link Sdk.GameServer as the Reserved state for Duration.
     *
     * @param duration the duration to mark.
     *
     * @return A future that represents the result of the reserve operation.
     */
    default CompletableFuture<Sdk.Empty> reserveFuture(final Duration duration) {
        return Internal.observerToFuture(response -> this.reserve(duration, response));
    }

    /**
     * Call when the @link Sdk.GameServer is shutting down.
     *
     * @param response the response from server.
     */
    void shutdown(StreamObserver<Sdk.Empty> response);

    /**
     * Call when the @link Sdk.GameServer is shutting down.
     */
    default void shutdown() {
        this.shutdown(Internal.observerEmpty());
    }

    /**
     * Call when the @link Sdk.GameServer is shutting down.
     *
     * @return A future that represents the result of the shutdown operation.
     */
    default CompletableFuture<Sdk.Empty> shutdownFuture() {
        return Internal.observerToFuture(this::shutdown);
    }

    /**
     * Apply an Annotation to the backing @link Sdk.GameServer metadata.
     *
     * @param key the key to apply.
     * @param value the value to apply.
     * @param observer the observer to apply.
     */
    void setAnnotation(String key, String value, StreamObserver<Sdk.Empty> observer);

    /**
     * Apply an Annotation to the backing @link Sdk.GameServer metadata.
     *
     * @param key the key to apply.
     * @param value the value to apply.
     */
    default void setAnnotation(final String key, final String value) {
        this.setAnnotation(key, value, Internal.observerEmpty());
    }

    /**
     * Apply an Annotation to the backing @link Sdk.GameServer metadata.
     *
     * @param key the key to apply.
     * @param value the value to apply.
     *
     * @return A future that represents the result of the set annotation operation.
     */
    default CompletableFuture<Sdk.Empty> setAnnotationFuture(final String key, final String value) {
        return Internal.observerToFuture(response -> this.setAnnotation(key, value, response));
    }

    /**
     * Apply a Label to the backing @link Sdk.GameServer metadata.
     *
     * @param key the key to apply.
     * @param value the value to apply.
     * @param response the response from server.
     */
    void setLabel(String key, String value, StreamObserver<Sdk.Empty> response);

    /**
     * Apply a Label to the backing @link Sdk.GameServer metadata.
     *
     * @param key the key to apply.
     * @param value the value to apply.
     */
    default void setLabel(final String key, final String value) {
        this.setLabel(key, value, Internal.observerEmpty());
    }

    /**
     * Apply a Label to the backing @link Sdk.GameServer metadata.
     *
     * @param key the key to apply.
     * @param value the value to apply.
     *
     * @return A future that represents the result of the set label operation.
     */
    default CompletableFuture<Sdk.Empty> setLabelFuture(final String key, final String value) {
        return Internal.observerToFuture(response -> this.setLabel(key, value, response));
    }

    /**
     * Returns the list of the currently connected player ids.
     * <p>
     * This is always accurate from what has been set through this SDK, even if the value has yet to be updated on the
     * @link Sdk.GameServer status resource.
     * <p>
     * If GameServer#Status#PlayerStatus#getIdsList is set manually through the Kubernetes API, use
     * {@link Agones#getGameServerFuture()} or {@link Agones#addGameServerWatcher(Consumer)} instead to view this value.
     *
     * @param response the response from server.
     */
    void getConnectedPlayersFuture(StreamObserver<List<String>> response);

    /**
     * Returns the list of the currently connected player ids.
     * <p>
     * This is always accurate from what has been set through this SDK, even if the value has yet to be updated on the
     * @link Sdk.GameServer status resource.
     * <p>
     * If GameServer#Status#PlayerStatus#getIdsList is set manually through the Kubernetes API, use
     * {@link Agones#getGameServerFuture()} or {@link Agones#addGameServerWatcher(Consumer)} instead to view this value.
     *
     * @return A future that represents the list of the currently connected player ids.
     */
    default CompletableFuture<List<String>> getConnectedPlayersFuture() {
        return Internal.observerToFuture(this::getConnectedPlayersFuture);
    }

    /**
     * Increases the SDK’s stored player count by one, and appends this playerID to GameServer#Status#PlayerStatus#getIdsList.
     * <p>
     * GameServer#Status#PlayerStatus#getCount and GameServer#Status#PlayerStatus#getIdsList are then set to update the player count and id list a second from now, unless there is already an update pending, in which case the update joins that batch operation.
     * <p>
     * The response returns true and adds the playerID to the list of playerIDs if this playerID was not already in the list of connected playerIDs.
     * <p>
     * If the playerID exists within the list of connected playerIDs, will return false, and the list of connected playerIDs will be left unchanged.
     * <p>
     * An error will be returned if the playerID was not already in the list of connected playerIDs but the player capacity for the server has been reached.
     * The playerID will not be added to the list of playerIDs.
     * <p>
     * WARNING: Do not use this method if you are manually managing GameServer#Status#PlayerStatus#getIdsList and GameServer#Status#PlayerStatus#getCount through the Kubernetes API, as indeterminate results will occur.
     *
     * @param playerId the player id to connect.
     * @param response the response from server.
     */
    void playerConnect(String playerId, StreamObserver<Boolean> response);

    /**
     * Increases the SDK’s stored player count by one, and appends this playerID to GameServer#Status#PlayerStatus#getIdsList.
     * GameServer#Status#PlayerStatus#getCount and GameServer#Status#PlayerStatus#getIdsList are then set to update the player count and id list a second from now, unless there is already an update pending, in which case the update joins that batch operation.
     * <p>
     * If the playerID exists within the list of connected playerIDs, will return false, and the list of connected playerIDs will be left unchanged.
     * <p>
     * An error will be returned if the playerID was not already in the list of connected playerIDs but the player capacity for the server has been reached.
     * The playerID will not be added to the list of playerIDs.
     * <p>
     * WARNING: Do not use this method if you are manually managing GameServer#Status#PlayerStatus#getIdsList and GameServer#Status#PlayerStatus#getCount through the Kubernetes API, as indeterminate results will occur.
     *
     * @param playerId the player id to connect.
     *
     * @return A future that represents the result of the player connect operation.
     */
    default CompletableFuture<Boolean> playerConnectFuture(final String playerId) {
        return Internal.observerToFuture(response -> this.playerConnect(playerId, response));
    }

    /**
     * Decreases the SDK’s stored player count by one, and removes the playerID from GameServer#Status#PlayerStatus#getIdsList
     * GameServer#Status#PlayerStatus#getCount and GameServer#Status#PlayerStatus#getIdsList are then set to update the player count and id list a second from now, unless there is already an update pending, in which case the update joins that batch operation.
     * <p>
     * The response returns true and removes the playerID from the list of connected playerIDs if the playerID value exists within the list.
     * <p>
     * If the playerID was not in the list of connected playerIDs, the call will return false, and the connected playerID list will be left unchanged.
     * <p>
     * WARNING: Do not use this method if you are manually managing GameServer#Status#PlayerStatus#getIdsList and GameServer#Status#PlayerStatus#getCount through the Kubernetes API, as indeterminate results will occur.
     *
     * @param playerId the player id to disconnect.
     * @param response the response from server.
     */
    void playerDisconnect(String playerId, StreamObserver<Boolean> response);

    /**
     * Decreases the SDK’s stored player count by one, and removes the playerID from GameServer#Status#PlayerStatus#getIdsList
     * GameServer#Status#PlayerStatus#getCount and GameServer#Status#PlayerStatus#getIdsList are then set to update the player count and id list a second from now, unless there is already an update pending, in which case the update joins that batch operation.
     * <p>
     * If the playerID was not in the list of connected playerIDs, the call will return false, and the connected playerID list will be left unchanged.
     * <p>
     * WARNING: Do not use this method if you are manually managing GameServer#Status#PlayerStatus#getIdsList and GameServer#Status#PlayerStatus#getCount through the Kubernetes API, as indeterminate results will occur.
     *
     * @param playerId the player id to disconnect.
     *
     * @return A future that represents the result of the player disconnect operation.
     */
    default CompletableFuture<Boolean> playerDisconnectFuture(final String playerId) {
        return Internal.observerToFuture(response -> this.playerDisconnect(playerId, response));
    }

    /**
     * Returns if the playerID is currently connected to the @link Sdk.GameServer.
     * <p>
     * This is always accurate from what has been set through this SDK, even if the value has yet to be updated on the @link Sdk.GameServer status resource.
     * <p>
     * If GameServer#Status#PlayerStatus#getIdsList is set manually through the Kubernetes API, use {@link Agones#getGameServerFuture()} or {@link Agones#addGameServerWatcher(Consumer)} instead to determine connected status.
     *
     * @param playerId the player id to return.
     * @param response the response from server.
     */
    void isPlayerConnected(String playerId, StreamObserver<Boolean> response);

    /**
     * Returns if the playerID is currently connected to the @link Sdk.GameServer.
     * <p>
     * This is always accurate from what has been set through this SDK, even if the value has yet to be updated on the @link Sdk.GameServer status resource.
     * <p>
     * If GameServer#Status#PlayerStatus#getIdsList is set manually through the Kubernetes API, use {@link Agones#getGameServerFuture()} or {@link Agones#addGameServerWatcher(Consumer)} instead to determine connected status.
     *
     * @param playerId the player id to return.
     *
     * @return A future that represents the result of the is player connected operation.
     */
    default CompletableFuture<Boolean> isPlayerConnected(final String playerId) {
        return Internal.observerToFuture(response -> this.isPlayerConnected(playerId, response));
    }

    /**
     * Update the GameServer#Status#PlayerStatus#getCapacity value with a new capacity.
     *
     * @param capacity the capacity to update.
     * @param response the response from server.
     */
    void setPlayerCapacity(long capacity, StreamObserver<Alpha.Empty> response);

    /**
     * Update the GameServer#Status#PlayerStatus#getCapacity value with a new capacity.
     *
     * @param capacity the capacity to update.
     *
     * @return A future that represents the result of the set player capacity operation.
     */
    default CompletableFuture<Alpha.Empty> setPlayerCapacityFuture(final long capacity) {
        return Internal.observerToFuture(response -> this.setPlayerCapacity(capacity, response));
    }

    /**
     * Retrieves the current player capacity.
     * <p>
     * This is always accurate from what has been set through this SDK, even if the value has yet to be updated on the @link Sdk.GameServer status resource.
     * <p>
     * If GameServer#Status#PlayerStatus#getCapacity is set manually through the Kubernetes API, use {@link Agones#getGameServerFuture()} or {@link Agones#addGameServerWatcher(Consumer)} instead to view this value.
     *
     * @param response the response from server.
     */
    void getPlayerCapacity(StreamObserver<Long> response);

    /**
     * Retrieves the current player capacity.
     * <p>
     * This is always accurate from what has been set through this SDK, even if the value has yet to be updated on the @link Sdk.GameServer status resource.
     * <p>
     * If GameServer#Status#PlayerStatus#getCapacity is set manually through the Kubernetes API, use {@link Agones#getGameServerFuture()} or {@link Agones#addGameServerWatcher(Consumer)} instead to view this value.
     *
     * @return A future that represents the current player capacity.
     */
    default CompletableFuture<Long> getPlayerCapacityFuture() {
        return Internal.observerToFuture(this::getPlayerCapacity);
    }

    /**
     * Retrieves the current player count.
     * <p>
     * This is always accurate from what has been set through this SDK, even if the value has yet to be updated on the @link Sdk.GameServer status resource.
     * <p>
     * If GameServer#Status#PlayerStatus#getCount is set manually through the Kubernetes API, use {@link Agones#getGameServerFuture()} or {@link Agones#addGameServerWatcher(Consumer)} instead to view this value.
     *
     * @param response the response from server.
     */
    void getPlayerCount(StreamObserver<Long> response);

    /**
     * Retrieves the current player count.
     * <p>
     * This is always accurate from what has been set through this SDK, even if the value has yet to be updated on the @link Sdk.GameServer status resource.
     * <p>
     * If GameServer#Status#PlayerStatus#getCount is set manually through the Kubernetes API, use {@link Agones#getGameServerFuture()} or {@link Agones#addGameServerWatcher(Consumer)} instead to view this value.
     *
     * @return A future that represents the current player count.
     */
    default CompletableFuture<Long> getPlayerCountFuture() {
        return Internal.observerToFuture(this::getPlayerCount);
    }

    /**
     * gets a list.
     * <p>
     * returns NOT_FOUND if the List does not exist.
     *
     * @param name the name to get.
     * @param response the response from server.
     */
    void getList(String name, StreamObserver<AgonesList> response);

    /**
     * gets a list.
     * <p>
     * Returns NOT_FOUND if the List does not exist.
     *
     * @param name the name to get.
     *
     * @return the list.
     */
    default CompletableFuture<AgonesList> getList(final String name) {
        return Internal.observerToFuture(response -> this.getList(name, response));
    }

    /**
     * Adds a value to a list and returns updated list.
     * <p>
     * Returns NOT_FOUND if the list does not exist.
     * <p>
     * Returns ALREADY_EXISTS if the value is already in the list.
     * <p>
     * Returns OUT_OF_RANGE if the list is already at Capacity.
     *
     * @param name the name to add.
     * @param value the value to add.
     * @param response the response from server.
     */
    void addList(String name, String value, StreamObserver<AgonesList> response);

    /**
     * Adds a value to a list and returns updated list.
     * <p>
     * Returns NOT_FOUND if the list does not exist.
     * <p>
     * Returns ALREADY_EXISTS if the value is already in the list.
     * <p>
     * Returns OUT_OF_RANGE if the list is already at Capacity.
     *
     * @param name the name to add.
     * @param value the value to add.
     *
     * @return the updated list.
     */
    default CompletableFuture<AgonesList> addListFuture(final String name, final String value) {
        return Internal.observerToFuture(response -> this.addList(name, value, response));
    }

    /**
     * Removes a value from a list and returns updated list.
     * <p>
     * Returns NOT_FOUND if the list does not exist.
     * <p>
     * Returns NOT_FOUND if the value is not in the list.
     *
     * @param name the name to remove.
     * @param value the value to remove.
     * @param response the response from server.
     */
    void removeList(String name, String value, StreamObserver<AgonesList> response);

    /**
     * Removes a value from a list and returns updated list.
     * <p>
     * Returns NOT_FOUND if the list does not exist.
     * <p>
     * Returns NOT_FOUND if the value is not in the list.
     *
     * @param name the name to remove.
     * @param value the value to remove.
     *
     * @return the updated list.
     */
    default CompletableFuture<AgonesList> removeListFuture(final String name, final String value) {
        return Internal.observerToFuture(response -> this.removeList(name, value, response));
    }

    /**
     * Returns the updated list.
     * <p>
     * Returns NOT_FOUND if the list does not exist (name cannot be updated).
     * <p>
     * THIS WILL OVERWRITE ALL EXISTING LIST.VALUES WITH ANY REQUEST LIST.VALUES.
     * <p>
     * Use {@link #addListFuture(String, String)} or {@link #removeListFuture(String, String)} for modifying the
     * List.Values field.
     * <p>
     * Returns INVALID_ARGUMENT if the field mask path(s) are not field(s) of the list.
     *
     * @param list the list to update.
     * @param updateMask the update mask to update.
     * @param response the response from server.
     */
    void updateList(AgonesList list, List<String> updateMask, StreamObserver<AgonesList> response);

    /**
     * Returns the updated list.
     * <p>
     * Returns NOT_FOUND if the list does not exist (name cannot be updated).
     * <p>
     * THIS WILL OVERWRITE ALL EXISTING LIST.VALUES WITH ANY REQUEST LIST.VALUES.
     * <p>
     * Use {@link #addListFuture(String, String)} or {@link #removeListFuture(String, String)} for modifying the
     * List.Values field.
     * <p>
     * Returns INVALID_ARGUMENT if the field mask path(s) are not field(s) of the list.
     *
     * @param list the list to update.
     * @param updateMask the update mask to update.
     *
     * @return the updated list.
     */
    default CompletableFuture<AgonesList> updateList(
        final AgonesList list,
        final List<String> updateMask
    ) {
        return Internal.observerToFuture(response -> this.updateList(list, updateMask, response));
    }

    /**
     * gets a counter.
     * <p>
     * Returns NOT_FOUND if the counter does not exist.
     *
     * @param name the name of the counter.
     * @param response the response from server.
     */
    void getCounter(String name, StreamObserver<AgonesCounter> response);

    /**
     * gets a counter.
     * <p>
     * Returns NOT_FOUND if the counter does not exist.
     *
     * @param name the name of the counter.
     *
     * @return the counter.
     */
    default CompletableFuture<AgonesCounter> getCounterFuture(final String name) {
        return Internal.observerToFuture(response -> this.getCounter(name, response));
    }

    /**
     * Increases the count of the Counter by the specified amount.
     * <p>
     * Returns NOT_FOUND if the Counter does not exist (name cannot be updated).
     * <p>
     * Returns OUT_OF_RANGE if the Count is out of range [0,Capacity].
     *
     * @param name the name of the counter.
     * @param amount the amount to increase.
     * @param response the response from server.
     */
    void increaseCounter(String name, long amount, StreamObserver<AgonesCounter> response);

    /**
     * Increases the count of the Counter by the specified amount.
     * <p>
     * Returns NOT_FOUND if the Counter does not exist (name cannot be updated).
     * <p>
     * Returns OUT_OF_RANGE if the Count is out of range [0,Capacity].
     *
     * @param name the name of the counter.
     * @param amount the amount to increase.
     *
     * @return the counter.
     */
    default CompletableFuture<AgonesCounter> increaseCounterFuture(
        final String name,
        final long amount
    ) {
        return Internal.observerToFuture(response -> this.increaseCounter(name, amount, response));
    }

    /**
     * Decreases the count of the Counter by the specified amount.
     * <p>
     * Returns NOT_FOUND if the Counter does not exist (name cannot be updated).
     * <p>
     * Returns OUT_OF_RANGE if the Count is out of range [0,Capacity].
     *
     * @param name the name of the counter.
     * @param amount the amount to decrease.
     * @param response the response from server.
     */
    void decreaseCounter(String name, long amount, StreamObserver<AgonesCounter> response);

    /**
     * Decreases the count of the Counter by the specified amount.
     * <p>
     * Returns NOT_FOUND if the Counter does not exist (name cannot be updated).
     * <p>
     * Returns OUT_OF_RANGE if the Count is out of range [0,Capacity].
     *
     * @param name the name of the counter.
     * @param amount the amount to decrease.
     *
     * @return the counter.
     */
    default CompletableFuture<AgonesCounter> decreaseCounter(final String name, final long amount) {
        return Internal.observerToFuture(response -> this.decreaseCounter(name, amount, response));
    }

    /**
     * Sets the count of the Counter by the specified amount.
     * <p>
     * Returns NOT_FOUND if the Counter does not exist (name cannot be updated).
     * <p>
     * Returns OUT_OF_RANGE if the Count is out of range [0,Capacity].
     *
     * @param name the name of the counter.
     * @param amount the amount to set.
     * @param response the response from server.
     */
    void setCounterCount(String name, long amount, StreamObserver<AgonesCounter> response);

    /**
     * Sets the count of the Counter by the specified amount.
     * <p>
     * Returns NOT_FOUND if the Counter does not exist (name cannot be updated).
     * <p>
     * Returns OUT_OF_RANGE if the Count is out of range [0,Capacity].
     *
     * @param name the name of the counter.
     * @param amount the amount to set.
     *
     * @return the counter.
     */
    default CompletableFuture<AgonesCounter> setCounterCountFuture(String name, long amount) {
        return Internal.observerToFuture(response -> this.setCounterCount(name, amount, response));
    }

    /**
     * Sets the capacity of the Counter by the specified amount.
     * <p>
     * 0 is no limit.
     * <p>
     * Returns NOT_FOUND if the Counter does not exist (name cannot be updated).
     *
     * @param name the name of the counter.
     * @param amount the amount to set.
     * @param response the response from server.
     */
    void setCounterCapacity(String name, long amount, StreamObserver<AgonesCounter> response);

    /**
     * Sets the capacity of the Counter by the specified amount.
     * <p>
     * 0 is no limit.
     * <p>
     * Returns NOT_FOUND if the Counter does not exist (name cannot be updated).
     *
     * @param name the name of the counter.
     * @param amount the amount to set.
     *
     * @return the counter.
     */
    default CompletableFuture<AgonesCounter> setCounterCapacityFuture(
        final String name,
        final long amount
    ) {
        return Internal.observerToFuture(
            response -> this.setCounterCapacity(name, amount, response)
        );
    }

    /**
     * A builder for creating instances of Agones with specified configurations.
     */
    interface Builder {
        /**
         * Builds an instance of Agones using the specified configuration.
         *
         * @return An instance of Agones.
         */
        Agones build();

        /**
         * Sets the address for the Agones Builder.
         *
         * @param host The host address to be set.
         * @param port The port number to be set.
         *
         * @return The Agones Builder instance.
         *
         * @see ManagedChannelBuilder#forAddress(String, int)
         * @see ManagedChannelBuilder#usePlaintext()
         */
        default Builder withAddress(final String host, final int port) {
            return this.withChannel(
                    ManagedChannelBuilder.forAddress(host, port).usePlaintext().build()
                );
        }

        /**
         * Sets the address for the Agones Builder.
         *
         * @param host The host address to be set.
         *
         * @return The Agones Builder instance.
         */
        default Builder withAddress(final String host) {
            return this.withAddress(host, Internal.GRPC_PORT);
        }

        /**
         * Sets the address for the Agones Builder.
         *
         * @param port The port number to be set.
         *
         * @return The Agones Builder instance.
         */
        default Builder withAddress(final int port) {
            return this.withAddress(Internal.GRPC_HOST, port);
        }

        /**
         * Sets the address for the Agones Builder.
         *
         * @return The Agones Builder instance.
         */
        default Builder withAddress() {
            return this.withAddress(Internal.GRPC_HOST);
        }

        /**
         * Sets the target for the Agones Builder.
         *
         * @param target The target to be set.
         *
         * @return The Agones Builder instance.
         *
         * @see ManagedChannelBuilder#forTarget(String)
         * @see ManagedChannelBuilder#usePlaintext()
         */
        default Builder withTarget(final String target) {
            return this.withChannel(ManagedChannelBuilder.forTarget(target).usePlaintext().build());
        }

        /**
         * Sets the target for the Agones Builder.
         *
         * @return The Agones Builder instance.
         */
        default Builder withTarget() {
            Objects.requireNonNull(
                Internal.GRPC_ADDRESS,
                "Environment variable 'AGONES_SDK_GRPC_ADDRESS' is not set!"
            );
            return this.withTarget(Internal.GRPC_ADDRESS);
        }

        /**
         * Sets the channel from environment variables for the Agones Builder.
         *
         * @return The Agones Builder instance.
         */
        default Builder withChannel() {
            if (Internal.GRPC_ADDRESS == null) {
                return this.withAddress();
            } else {
                return this.withTarget();
            }
        }

        /**
         * Sets the ManagedChannel for the Agones Builder.
         *
         * @param channel The ManagedChannel to be set.
         *
         * @return The Agones Builder instance.
         */
        Builder withChannel(ManagedChannel channel);

        /**
         * Sets the executor for the game server watcher.
         *
         * @param executor The executor service to be used by the game server watcher. {@code null} to disable it. Disabled by default.
         *
         * @return The Agones Builder instance.
         */
        Builder withGameServerWatcherExecutor(ExecutorService executor);

        /**
         * Sets the health check interval for the Agones Builder.
         *
         * @param delay The delay duration for health checks. {@code null} to disable it. Disabled by default.
         * @param period The interval duration for health checks.
         *
         * @return The Agones Builder instance.
         */
        Builder withHealthCheck(Duration delay, Duration period);

        /**
         * Sets the health check executor for the builder.
         *
         * @param executor the scheduled executor service to be used for health checks. {@code null} to disable it. Default is {@link Executors#newSingleThreadScheduledExecutor()}
         *
         * @return The Agones Builder instance.
         */
        Builder withHealthCheckExecutor(ScheduledExecutorService executor);
    }
}
