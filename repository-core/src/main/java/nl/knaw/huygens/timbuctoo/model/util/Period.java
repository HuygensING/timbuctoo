package nl.knaw.huygens.timbuctoo.model.util;

public class Period {

  private Datable startDate;
  private Datable endDate;

  public Period() {}

  public Datable getStartDate() {
    return startDate;
  }

  public void setStartDate(Datable startDate) {
    this.startDate = startDate;
  }

  public Datable getEndDate() {
    return endDate;
  }

  public void setEndDate(Datable endDate) {
    this.endDate = endDate;
  }

}
