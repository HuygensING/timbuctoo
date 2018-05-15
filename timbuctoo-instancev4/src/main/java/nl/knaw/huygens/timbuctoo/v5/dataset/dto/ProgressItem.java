package nl.knaw.huygens.timbuctoo.v5.dataset.dto;

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

  public void start() {
    startMoment = Instant.now();
    status = ImportStatusLabel.IMPORTING;
  }

  public void update(long numberOfTriplesProcessed) {
    this.numberOfTriplesProcessed = numberOfTriplesProcessed;
  }

  public void finish() {
    status = ImportStatusLabel.DONE;
  }

  public ImportStatusLabel getStatus() {
    return status;
  }

  public String getProgress() {
    return String.format("%d quads", numberOfTriplesProcessed);
  }

  public String getSpeed() {
    long duration = Duration.between(Instant.now(), startMoment).get(ChronoUnit.SECONDS);
    return String.format("%d quads/s", numberOfTriplesProcessed / duration);
  }
}
