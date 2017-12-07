package nl.knaw.huygens.timbuctoo.v5.dataset.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

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

  public String getTimeUnit() {
    return timeUnit;
  }

  public long getTime() {
    return time;
  }
}
