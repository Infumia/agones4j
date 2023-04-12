package tr.com.infumia.agones4j;

import agones.dev.sdk.Sdk;
import agones.dev.sdk.alpha.Alpha;
import agones.dev.sdk.alpha.SDKGrpc;
import com.google.protobuf.FieldMask;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents Agones Sdk.
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class AgonesAlphaSdk {

  /**
   * the stub.
   */
  @NotNull
  SDKGrpc.SDKStub stub;

  /**
   * ctor.
   *
   * @param channel the channel.
   */
  AgonesAlphaSdk(@NotNull final ManagedChannel channel) {
    this.stub = SDKGrpc.newStub(channel);
  }

  /**
   * adds a value to a list and returns updated list. returns NOT_FOUND if the list does not exist. returns
   * ALREADY_EXISTS if the value is already in the list. returns OUT_OF_RANGE if the list is already at Capacity.
   *
   * @param name the name to add.
   * @param value the value to add.
   * @param observer the observer to add.
   */
  public void addList(
    @NotNull final String name,
    @NotNull final String value,
    @NotNull final StreamObserver<Alpha.List> observer
  ) {
    this.stub.addListValue(
        Alpha.AddListValueRequest
          .newBuilder()
          .setName(name)
          .setValue(value)
          .build(),
        observer
      );
  }

  /**
   * returns the list of the currently connected player ids.
   * this is always accurate from what has been set through this SDK, even if the value has yet to be updated on the
   * {@link Sdk.GameServer} status resource.
   * if {@link Sdk.GameServer.Status.PlayerStatus#getIdsList()} is set manually through the Kubernetes API, use
   * {@link AgonesSdk#getGameServer(StreamObserver)} or {@link AgonesSdk#watchGameServer(StreamObserver)} instead to
   * view this value.
   *
   * @param observer the observer to return.
   */
  public void getConnectedPlayers(
    @NotNull final StreamObserver<Alpha.PlayerIDList> observer
  ) {
    this.stub.getConnectedPlayers(Alpha.Empty.getDefaultInstance(), observer);
  }

  /**
   * gets a counter. returns NOT_FOUND if the counter does not exist.
   *
   * @param name the name of the counter.
   * @param observer the observer to get counter.
   */
  public void getCounter(
    @NotNull final String name,
    @NotNull final StreamObserver<Alpha.Counter> observer
  ) {
    this.stub.getCounter(
        Alpha.GetCounterRequest.newBuilder().setName(name).build(),
        observer
      );
  }

  /**
   * gets a list. returns NOT_FOUND if the List does not exist.
   *
   * @param name the name to get.
   * @param observer the observer to get.
   */
  public void getList(
    @NotNull final String name,
    @NotNull final StreamObserver<Alpha.List> observer
  ) {
    this.stub.getList(
        Alpha.GetListRequest.newBuilder().setName(name).build(),
        observer
      );
  }

  /**
   * retrieves the current player capacity.
   * this is always accurate from what has been set through this SDK, even if the value has yet to be updated on the
   * {@link Sdk.GameServer} status resource.
   * if {@link Sdk.GameServer.Status.PlayerStatus#getCapacity()} is set manually through the Kubernetes API, use
   * {@link AgonesSdk#getGameServer(StreamObserver)} or {@link AgonesSdk#watchGameServer(StreamObserver)} instead to
   * view this value.
   *
   * @param observer the observer to retrieve.
   */
  public void getPlayerCapacity(
    @NotNull final StreamObserver<Alpha.Count> observer
  ) {
    this.stub.getPlayerCapacity(Alpha.Empty.getDefaultInstance(), observer);
  }

  /**
   * retrieves the current player count.
   * this is always accurate from what has been set through this SDK, even if the value has yet to be updated on the
   * {@link Sdk.GameServer} status resource.
   * if {@link Sdk.GameServer.Status.PlayerStatus#getCount()} is set manually through the Kubernetes API, use
   * {@link AgonesSdk#getGameServer(StreamObserver)} or {@link AgonesSdk#watchGameServer(StreamObserver)} instead to
   * view this value.
   *
   * @param observer the observer to retrieve.
   */
  public void getPlayerCount(
    @NotNull final StreamObserver<Alpha.Count> observer
  ) {
    this.stub.getPlayerCount(Alpha.Empty.getDefaultInstance(), observer);
  }

  /**
   * returns if the playerID is currently connected to the {@link Sdk.GameServer}.
   * this is always accurate from what has been set through this SDK, even if the value has yet to be updated on the
   * {@link Sdk.GameServer} status resource.
   * if {@link Sdk.GameServer.Status.PlayerStatus#getIdsList()} is set manually through the Kubernetes API, use
   * {@link AgonesSdk#getGameServer(StreamObserver)} or {@link AgonesSdk#watchGameServer(StreamObserver)} instead to
   * determine connected status.
   *
   * @param playerId the player id to return.
   * @param observer the observer to return.
   */
  public void isPlayerConnected(
    @NotNull final Alpha.PlayerID playerId,
    @NotNull final StreamObserver<Alpha.Bool> observer
  ) {
    this.stub.isPlayerConnected(playerId, observer);
  }

  /**
   * returns if the playerID is currently connected to the {@link Sdk.GameServer}.
   * this is always accurate from what has been set through this SDK, even if the value has yet to be updated on the
   * {@link Sdk.GameServer} status resource.
   * if {@link Sdk.GameServer.Status.PlayerStatus#getIdsList()} is set manually through the Kubernetes API, use
   * {@link AgonesSdk#getGameServer(StreamObserver)} or {@link AgonesSdk#watchGameServer(StreamObserver)} instead to
   * determine connected status.
   *
   * @param playerId the player id to return.
   * @param observer the observer to return.
   */
  public void isPlayerConnected(
    @NotNull final String playerId,
    @NotNull final StreamObserver<Alpha.Bool> observer
  ) {
    this.isPlayerConnected(
        Alpha.PlayerID.newBuilder().setPlayerID(playerId).build(),
        observer
      );
  }

  /**
   * increases the SDK’s stored player count by one, and appends this playerID to
   * {@link Sdk.GameServer.Status.PlayerStatus#getIdsList()}.
   * {@link Sdk.GameServer.Status.PlayerStatus#getCount()} and {@link Sdk.GameServer.Status.PlayerStatus#getIdsList()}
   * are then set to update the player count and id list a second from now, unless there is already an update pending,
   * in which case the update joins that batch operation.
   * returns true and adds the playerID to the list of playerIDs if this playerID was not already in the list of
   * connected playerIDs.
   * if the playerID exists within the list of connected playerIDs, will return false, and the list of connected
   * playerIDs will be left unchanged.
   * an error will be returned if the playerID was not already in the list of connected playerIDs but the player
   * capacity for the server has been reached.
   * the playerID will not be added to the list of playerIDs.
   * warning: do not use this method if you are manually managing
   * {@link Sdk.GameServer.Status.PlayerStatus#getIdsList()} and {@link Sdk.GameServer.Status.PlayerStatus#getCount()}
   * through the Kubernetes API, as indeterminate results will occur.
   *
   * @param playerId the player id to connect.
   * @param observer the observer to connect.
   */
  public void playerConnect(
    @NotNull final Alpha.PlayerID playerId,
    @NotNull final StreamObserver<Alpha.Bool> observer
  ) {
    this.stub.playerConnect(playerId, observer);
  }

  /**
   * increases the SDK’s stored player count by one, and appends this playerID to
   * {@link Sdk.GameServer.Status.PlayerStatus#getIdsList()}.
   * {@link Sdk.GameServer.Status.PlayerStatus#getCount()} and {@link Sdk.GameServer.Status.PlayerStatus#getIdsList()}
   * are then set to update the player count and id list a second from now, unless there is already an update pending,
   * in which case the update joins that batch operation.
   * returns true and adds the playerID to the list of playerIDs if this playerID was not already in the list of
   * connected playerIDs.
   * if the playerID exists within the list of connected playerIDs, will return false, and the list of connected
   * playerIDs will be left unchanged.
   * an error will be returned if the playerID was not already in the list of connected playerIDs but the player
   * capacity for the server has been reached.
   * the playerID will not be added to the list of playerIDs.
   * warning: do not use this method if you are manually managing
   * {@link Sdk.GameServer.Status.PlayerStatus#getIdsList()} and {@link Sdk.GameServer.Status.PlayerStatus#getCount()}
   * through the Kubernetes API, as indeterminate results will occur.
   *
   * @param playerId the player id to connect.
   * @param observer the observer to connect.
   */
  public void playerConnect(
    @NotNull final String playerId,
    @NotNull final StreamObserver<Alpha.Bool> observer
  ) {
    this.playerConnect(
        Alpha.PlayerID.newBuilder().setPlayerID(playerId).build(),
        observer
      );
  }

  /**
   * decreases the SDK’s stored player count by one, and removes the playerID from
   * {@link Sdk.GameServer.Status.PlayerStatus#getIdsList()}.
   * {@link Sdk.GameServer.Status.PlayerStatus#getCount()} and {@link Sdk.GameServer.Status.PlayerStatus#getIdsList()}
   * are then set to update the player count and id list a second from now, unless there is already an update pending,
   * in which case the update joins that batch operation.
   * will return true and remove the supplied playerID from the list of connected playerIDs if the playerID value exists
   * within the list.
   * if the playerID was not in the list of connected playerIDs, the call will return false, and the connected playerID
   * list will be left unchanged.
   * warning: do not use this method if you are manually managing
   * {@link Sdk.GameServer.Status.PlayerStatus#getIdsList()} and {@link Sdk.GameServer.Status.PlayerStatus#getCount()}
   * through the Kubernetes API, as indeterminate results will occur.
   *
   * @param playerId the player id to disconnect.
   * @param observer the observer to disconnect.
   */
  public void playerDisconnect(
    @NotNull final Alpha.PlayerID playerId,
    @NotNull final StreamObserver<Alpha.Bool> observer
  ) {
    this.stub.playerDisconnect(playerId, observer);
  }

  /**
   * decreases the SDK’s stored player count by one, and removes the playerID from
   * {@link Sdk.GameServer.Status.PlayerStatus#getIdsList()}.
   * {@link Sdk.GameServer.Status.PlayerStatus#getCount()} and {@link Sdk.GameServer.Status.PlayerStatus#getIdsList()}
   * are then set to update the player count and id list a second from now, unless there is already an update pending,
   * in which case the update joins that batch operation.
   * will return true and remove the supplied playerID from the list of connected playerIDs if the playerID value exists
   * within the list.
   * if the playerID was not in the list of connected playerIDs, the call will return false, and the connected playerID
   * list will be left unchanged.
   * warning: do not use this method if you are manually managing
   * {@link Sdk.GameServer.Status.PlayerStatus#getIdsList()} and {@link Sdk.GameServer.Status.PlayerStatus#getCount()}
   * through the Kubernetes API, as indeterminate results will occur.
   *
   * @param playerId the player id to disconnect.
   * @param observer the observer to disconnect.
   */
  public void playerDisconnect(
    @NotNull final String playerId,
    @NotNull final StreamObserver<Alpha.Bool> observer
  ) {
    this.playerDisconnect(
        Alpha.PlayerID.newBuilder().setPlayerID(playerId).build(),
        observer
      );
  }

  /**
   * removes a value from a list and returns updated list. returns NOT_FOUND if the list does not exist. returns
   * NOT_FOUND if the value is not in the list.
   *
   * @param name the name to remove.
   * @param value the value to remove.
   * @param observer the observer to add.
   */
  public void removeList(
    @NotNull final String name,
    @NotNull final String value,
    @NotNull final StreamObserver<Alpha.List> observer
  ) {
    this.stub.removeListValue(
        Alpha.RemoveListValueRequest
          .newBuilder()
          .setName(name)
          .setValue(value)
          .build(),
        observer
      );
  }

  /**
   * update the {@link Sdk.GameServer.Status.PlayerStatus#getCapacity()} value with a new capacity.
   *
   * @param capacity the capacity to update.
   * @param observer the observer to update.
   */
  public void setPlayerCapacity(
    @NotNull final Alpha.Count capacity,
    @NotNull final StreamObserver<Alpha.Empty> observer
  ) {
    this.stub.setPlayerCapacity(capacity, observer);
  }

  /**
   * returns the updated counter. returns NOT_FOUND if the counter does not exist (name cannot be updated). returns
   * OUT_OF_RANGE if the count is out of range [0,Capacity]. returns INVALID_ARGUMENT if the field mask path(s) are not
   * field(s) of the counter. if a field mask path(s) is specified, but the value is not set in the request counter
   * object, then the default value for the variable will be set (i.e. 0 for "capacity" or "count").
   *
   * @param counter the counter to update.
   * @param updateMask the update mask to update.
   * @param observer the observer to update.
   */
  public void updateCounter(
    @NotNull final Alpha.Counter counter,
    @NotNull final FieldMask updateMask,
    @NotNull final StreamObserver<Alpha.Counter> observer
  ) {
    this.stub.updateCounter(
        Alpha.UpdateCounterRequest
          .newBuilder()
          .setCounter(counter)
          .setUpdateMask(updateMask)
          .build(),
        observer
      );
  }

  /**
   * returns the updated list. returns NOT_FOUND if the list does not exist (name cannot be updated).
   * <p>
   * **THIS WILL OVERWRITE ALL EXISTING LIST.VALUES WITH ANY REQUEST LIST.VALUES**
   * <p>
   * use addListValue() or removeListValue() for modifying the List.Values field.
   * returns INVALID_ARGUMENT if the field mask path(s) are not field(s) of the list.
   *
   * @param observer the observer to update.
   */
  public void updateList(
    @NotNull final Alpha.List list,
    @NotNull final FieldMask updateMask,
    @NotNull final StreamObserver<Alpha.List> observer
  ) {
    this.stub.updateList(
        Alpha.UpdateListRequest
          .newBuilder()
          .setList(list)
          .setUpdateMask(updateMask)
          .build(),
        observer
      );
  }
}
