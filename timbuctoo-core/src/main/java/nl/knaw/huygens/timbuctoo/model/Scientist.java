package nl.knaw.huygens.timbuctoo.model;

public class Scientist extends Role {
  private String fieldsOfInterest;

  public String getFieldsOfInterest() {
    return fieldsOfInterest;
  }

  public void setFieldsOfInterest(String fieldsOfInterest) {
    this.fieldsOfInterest = fieldsOfInterest;
  }
}
