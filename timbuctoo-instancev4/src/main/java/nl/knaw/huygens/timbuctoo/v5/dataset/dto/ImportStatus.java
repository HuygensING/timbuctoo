package nl.knaw.huygens.timbuctoo.v5.dataset.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class ImportStatus {

  @JsonProperty
  private String date;

  @JsonProperty
  private String status;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private TimeWithUnit elapsedTime;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<String> errors = new ArrayList<>();

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String importDate) {
    this.date = importDate;
  }

  public TimeWithUnit getElapsedTime() {
    return elapsedTime;
  }

  public void setElapsedTime(TimeWithUnit elapsedTime) {
    this.elapsedTime = elapsedTime;
  }

  public void setElapsedTimeMillis(long millis) {
    elapsedTime = new TimeWithUnit("MILLISECONDS", millis);
  }

  @JsonIgnore
  public List<String> getErrors() {
    return errors;
  }

  public void addError(String errorString) {
    errors.add(errorString);
  }

}
