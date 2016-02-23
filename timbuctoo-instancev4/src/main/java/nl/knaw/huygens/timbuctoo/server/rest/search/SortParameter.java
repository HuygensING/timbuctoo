package nl.knaw.huygens.timbuctoo.server.rest.search;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SortParameter {
  @JsonProperty("fieldname")
  private String fieldName;

  private Direction direction;

  public SortParameter(String fieldName, Direction direction) {
    this.fieldName = fieldName;
    this.direction = direction;
  }

  public SortParameter (){
    // constructor for Jackson
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public Direction getDirection() {
    return direction;
  }

  public void setDirection(Direction direction) {
    this.direction = direction;
  }

  public enum Direction {
    asc, desc
  }
}
