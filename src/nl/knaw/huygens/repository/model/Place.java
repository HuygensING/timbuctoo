package nl.knaw.huygens.repository.model;


public class Place {
  private String scriptorium = "";
  private String place = "";
  private String region = "";

  public String getScriptorium() {
    return scriptorium;
  }

  public void setScriptorium(String scriptorium) {
    this.scriptorium = scriptorium;
  }

  public String getPlace() {
    return place;
  }

  public void setPlace(String place) {
    this.place = place;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }
}
