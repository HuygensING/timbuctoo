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
    return super.toString();
  }
}
