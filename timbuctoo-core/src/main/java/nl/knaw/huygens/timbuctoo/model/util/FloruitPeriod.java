package nl.knaw.huygens.timbuctoo.model.util;

public class FloruitPeriod extends Period {
  public FloruitPeriod(String date) {
    super(date, date);
  }

  public FloruitPeriod() {}

  public FloruitPeriod(String startDate, String endDate) {
    super(startDate, endDate);
  }

  public FloruitPeriod(Datable date) {
    super(date, date);
  }

  public FloruitPeriod(Datable startDate, Datable endDate) {
    super(startDate, endDate);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("fl. ");

    int fromYear = getStartDate().getFromYear();
    int toYear = getEndDate().getToYear();
    sb.append(fromYear);

    if (fromYear == toYear) {

    } else {
      sb.append(" - ");
      sb.append(toYear);
    }

    return sb.toString();
  }
}
