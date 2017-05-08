package nl.knaw.huygens.timbuctoo.v5.datastores.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

public class StoreStatusImpl implements StoreStatus {

  private long currentVersion;
  private boolean isUpdating;
  private long position;
  private long finish;
  private Optional<String> currentError;

  public StoreStatusImpl(long currentVersion) {
    this.currentVersion = currentVersion;
  }

  @JsonCreator
  public StoreStatusImpl(@JsonProperty("currentVersion") long currentVersion,
                          @JsonProperty("updating") boolean isUpdating,
                          @JsonProperty("position") long position,
                          @JsonProperty("finish") long finish,
                          @JsonProperty("currentError") String currentError) {
    this.currentVersion = currentVersion;
    this.isUpdating = isUpdating;
    this.position = position;
    this.finish = finish;
    this.currentError = Optional.ofNullable(currentError);
  }

  @Override
  public long getCurrentVersion() {
    return currentVersion;
  }

  @Override
  public boolean isUpdating() {
    return isUpdating;
  }

  @Override
  public long getPosition() {
    return position;
  }

  @Override
  public long getFinish() {
    return finish;
  }

  @Override
  public Optional<String> getCurrentError() {
    return currentError;
  }

  public void startUpdate(long finish) {
    isUpdating = true;
    position = 0;
    this.finish = finish;
    currentError = Optional.empty();
  }

  public void finishUpdate(long newVersion) {
    isUpdating = false;
    position = 0;
    this.finish = 0;
    this.currentVersion = newVersion;
  }

  public void resetUpdate() {
    isUpdating = false;
    position = 0;
    this.finish = 0;
  }

  public void abortUpdate(String error) {
    currentError = Optional.empty();
  }

  public void setPosition(long position) {
    this.position = position;
  }
}
