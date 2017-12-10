package nl.knaw.huygens.timbuctoo.v5.dataset.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EntryImportStatus {

  @JsonProperty
  private String date;

  @JsonProperty
  private String status;

  private TimeWithUnit elapsedTime;

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

  public void setElapsedTime(TimeWithUnit elapsedTime) {
    this.elapsedTime = elapsedTime;
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public TimeWithUnit getElapsedTime() {
    return elapsedTime;
  }

  public long getElapsedTime(String unit) {
    if (elapsedTime != null) {
      return  TimeUnit.valueOf(unit).convert(elapsedTime.getTime(), TimeUnit.valueOf(elapsedTime.getTimeUnit()));
    } else {
      return -1L;
    }
  }

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public List<String> getErrors() {
    return errors;
  }

  public void addError(String errorString) {
    errors.add(errorString);
  }

}
