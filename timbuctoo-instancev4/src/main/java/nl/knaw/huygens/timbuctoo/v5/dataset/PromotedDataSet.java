package nl.knaw.huygens.timbuctoo.v5.dataset;

public class PromotedDataSet {
  private String name;
  private Boolean promoted;

  public PromotedDataSet() {

  }

  public PromotedDataSet(String name, Boolean promoted) {
    this.name = name;
    this.promoted = promoted;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Boolean getPromoted() {
    return promoted;
  }

  public void setPromoted(Boolean promoted) {
    this.promoted = promoted;
  }
}
