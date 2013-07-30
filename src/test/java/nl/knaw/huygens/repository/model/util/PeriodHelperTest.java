package nl.knaw.huygens.repository.model.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PeriodHelperTest {

  @Test
  public void createPeriodTwoYears() {
    assertEquals("2001 - 3001", PeriodHelper.createPeriod("2001", "3001"));
  }

  @Test
  public void createPeriodBeginNull() {
    assertEquals("3001 - 3001", PeriodHelper.createPeriod(null, "3001"));
  }

  @Test
  public void createPeriodBeginEmpty() {
    assertEquals("3001 - 3001", PeriodHelper.createPeriod("", "3001"));
  }

  @Test
  public void createPeriodBeginWhiteSpace() {
    assertEquals("3001 - 3001", PeriodHelper.createPeriod(" ", "3001"));
  }

  @Test
  public void createPeriodEndNull() {
    assertEquals("2001 - 2001", PeriodHelper.createPeriod("2001", null));
  }

  @Test
  public void createPeriodEndEmpty() {
    assertEquals("2001 - 2001", PeriodHelper.createPeriod("2001", ""));
  }

  @Test
  public void createPeriodEndWhiteSpace() {
    assertEquals("2001 - 2001", PeriodHelper.createPeriod("2001", " "));
  }

  @Test
  public void createPeriodBothNull() {
    assertEquals(null, PeriodHelper.createPeriod(null, null));
  }

  @Test
  public void createPeriodBothEmptyString() {
    assertEquals(null, PeriodHelper.createPeriod("", ""));
  }

  @Test
  public void createPeriodBothWiteSpace() {
    assertEquals(null, PeriodHelper.createPeriod(" ", " "));
  }
}
