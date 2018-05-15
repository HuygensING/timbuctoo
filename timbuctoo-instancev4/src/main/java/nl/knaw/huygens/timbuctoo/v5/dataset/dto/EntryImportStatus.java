package nl.knaw.huygens.timbuctoo.v5.dataset.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static nl.knaw.huygens.timbuctoo.v5.dataset.dto.ImportStatusLabel.IMPORTING;

public class EntryImportStatus {
  @JsonProperty
  private Map<String, ProgressItem> progressItemMap = Maps.newHashMap();

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
      return elapsedTime.getTime(unit);
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

  public void addProgressItem(String itemName, ImportStatusLabel statusLabel) {
    ProgressItem progressItem = new ProgressItem();
    progressItemMap.put(itemName, progressItem);
    if (statusLabel == IMPORTING) {
      progressItem.start();
    }
  }

  public void updateProgressItem(String itemName, long numberOfTriplesProcessed) {
    ProgressItem progressItem = progressItemMap.get(itemName);
    if (progressItem != null) {
      progressItem.update(numberOfTriplesProcessed);
    }
  }

  public void finishProgressItem(String itemName) {
    ProgressItem progressItem = progressItemMap.get(itemName);
    if (progressItem != null) {
      progressItem.finish();
    }
  }

  public void startProgressItem(String itemName) {
    ProgressItem progressItem = progressItemMap.get(itemName);
    if (progressItem != null) {
      progressItem.start();
    }
  }

  public Map<String, ProgressItem> getProgressItems() {
    return progressItemMap;
  }
}
