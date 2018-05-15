package nl.knaw.huygens.timbuctoo.v5.dataset.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;


public class ProgressItem {

  @JsonProperty
  private Instant startMoment;
  @JsonProperty
  private long numberOfTriplesProcessed;
  @JsonProperty
  private ImportStatusLabel status = ImportStatusLabel.PENDING;

  public ProgressItem() {

  }

  public synchronized void start() {
    startMoment = Instant.now();
    status = ImportStatusLabel.IMPORTING;
  }

  public synchronized void update(long numberOfTriplesProcessed) {
    this.numberOfTriplesProcessed = numberOfTriplesProcessed;
  }

  public synchronized void finish() {
    status = ImportStatusLabel.DONE;
  }

  @JsonIgnore
  public synchronized ImportStatusLabel getStatus() {
    return status;
  }

  @JsonIgnore
  public synchronized String getProgress() {
    return String.format("%d quads", numberOfTriplesProcessed);
  }

  @JsonIgnore
  public synchronized String getSpeed() {
    return String.format("%d quads/s", numberOfTriplesProcessed / duration);
  }
}
