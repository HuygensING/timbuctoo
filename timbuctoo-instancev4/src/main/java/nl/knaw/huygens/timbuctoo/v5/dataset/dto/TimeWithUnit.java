package nl.knaw.huygens.timbuctoo.v5.dataset.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.concurrent.TimeUnit;

public class TimeWithUnit {

  @JsonProperty
  private String timeUnit;

  @JsonProperty
  private long time;

  public TimeWithUnit() {

  }

  public TimeWithUnit(String timeUnit, long time) {
    this.timeUnit = timeUnit;
    this.time = time;
  }

  public TimeWithUnit withMilliseconds(long time) {
    timeUnit = TimeUnit.MILLISECONDS.name();
    this.time = time;
    return this;
  }

  public TimeWithUnit withSeconds(long time) {
    timeUnit = TimeUnit.SECONDS.name();
    this.time = time;
    return this;
  }

  public String getTimeUnit() {
    return timeUnit;
  }

  public long getTime() {
    return time;
  }
}
