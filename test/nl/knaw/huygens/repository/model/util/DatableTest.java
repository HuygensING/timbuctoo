package nl.knaw.huygens.repository.model.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

public class DatableTest {

  private void assertDate(String expectedAsText, Date date) {
    try {
      DateFormat format = new SimpleDateFormat("yyyy-MM-dd:HH");
      format.setTimeZone(Datable.CET);
      Date expected = format.parse(expectedAsText);
      Assert.assertEquals(expectedAsText, expected, date);
    } catch (ParseException e) {
      Assert.fail(e.getMessage());
    }
  }

  private void testDatable(String fromDate, String toDate, Datable.Certainty certainty, Datable datable) {
    assertDate(fromDate, datable.getFromDate());
    assertDate(toDate, datable.getToDate());
    Assert.assertEquals(certainty, datable.getCertainty());
  }

  @Test
  public void testTimeZone() {
    Assert.assertEquals(60 * 60 * 1000, Datable.CET.getRawOffset());
  }

  @Test
  public void convertYEAR() {
    Assert.assertTrue(EDTFPattern.YEAR.matches("2010"));
    Assert.assertFalse(EDTFPattern.YEAR.matches("201?"));
    Assert.assertFalse(EDTFPattern.YEAR.matches("2010-02-13"));
    testDatable("2010-01-01:12", "2010-12-31:12", Datable.Certainty.HIGH, new Datable("2010"));
  }

  @Test
  public void convertYEAR_Q() {
    Assert.assertTrue(EDTFPattern.YEAR_Q.matches("2010?"));
    Assert.assertFalse(EDTFPattern.YEAR_Q.matches("2010"));
    Assert.assertFalse(EDTFPattern.YEAR_Q.matches("2010-02-13?"));
    testDatable("2010-01-01:12", "2010-12-31:12", Datable.Certainty.LOW, new Datable("2010?"));
  }

  @Test
  public void convertYEAR_A() {
    Assert.assertTrue(EDTFPattern.YEAR_A.matches("2010~"));
    Assert.assertFalse(EDTFPattern.YEAR_A.matches("2010"));
    Assert.assertFalse(EDTFPattern.YEAR_A.matches("2010-02-13~"));
    testDatable("2010-01-01:12", "2010-12-31:12", Datable.Certainty.MEDIUM, new Datable("2010~"));
  }

  @Test
  public void convertYEAR_RANGE_Q1() {
    Assert.assertTrue(EDTFPattern.YEAR_RANGE_Q1.matches("201?"));
    Assert.assertFalse(EDTFPattern.YEAR_RANGE_Q1.matches("2010"));
    Assert.assertFalse(EDTFPattern.YEAR_RANGE_Q1.matches("201?-02-13"));
    testDatable("2010-01-01:12", "2019-12-31:12", Datable.Certainty.LOW, new Datable("201?"));
  }

  @Test
  public void convertYEAR_RANGE_Q2() {
    Assert.assertTrue(EDTFPattern.YEAR_RANGE_Q2.matches("20??"));
    Assert.assertFalse(EDTFPattern.YEAR_RANGE_Q2.matches("2010"));
    Assert.assertFalse(EDTFPattern.YEAR_RANGE_Q2.matches("20??-02-13"));
    testDatable("2000-01-01:12", "2099-12-31:12", Datable.Certainty.LOW, new Datable("20??"));
  }

  @Test
  public void convertYEAR_RANGE_Q3() {
    Assert.assertTrue(EDTFPattern.YEAR_RANGE_Q3.matches("2???"));
    Assert.assertFalse(EDTFPattern.YEAR_RANGE_Q3.matches("2010"));
    Assert.assertFalse(EDTFPattern.YEAR_RANGE_Q3.matches("2???-02-13"));
    testDatable("2000-01-01:12", "2999-12-31:12", Datable.Certainty.LOW, new Datable("2???"));
  }

  @Test
  public void convertYEAR_MONTH() {
    Assert.assertTrue(EDTFPattern.YEAR_MONTH.matches("2001-01"));
    Assert.assertFalse(EDTFPattern.YEAR_MONTH.matches("2010"));
    Assert.assertFalse(EDTFPattern.YEAR_MONTH.matches("2???-02-13"));
    testDatable("2010-02-01:12", "2010-02-28:12", Datable.Certainty.HIGH, new Datable("2010-02"));
    testDatable("1610-12-01:12", "1610-12-31:12", Datable.Certainty.HIGH, new Datable("1610-12"));
  }

  @Test
  public void convertYEAR_MONTH_Q() {
    Assert.assertTrue(EDTFPattern.YEAR_MONTH_Q.matches("2001-01?"));
    Assert.assertFalse(EDTFPattern.YEAR_MONTH_Q.matches("2010"));
    Assert.assertFalse(EDTFPattern.YEAR_MONTH_Q.matches("2???-02-13"));
    testDatable("2010-03-01:12", "2010-03-31:12", Datable.Certainty.LOW, new Datable("2010-03?"));
  }

  @Test
  public void convertYEAR_MONTH_A() {
    Assert.assertTrue(EDTFPattern.YEAR_MONTH_A.matches("2001-01~"));
    Assert.assertFalse(EDTFPattern.YEAR_MONTH_A.matches("2010"));
    Assert.assertFalse(EDTFPattern.YEAR_MONTH_A.matches("2???-02-13"));
    testDatable("1000-05-01:12", "1000-05-31:12", Datable.Certainty.MEDIUM, new Datable("1000-05~"));
  }

  @Test
  public void convertYEAR_MONTH_RANGE() {
    Assert.assertTrue(EDTFPattern.YEAR_MONTH_RANGE.matches("2010-??"));
    Assert.assertFalse(EDTFPattern.YEAR_MONTH_RANGE.matches("201?"));
    Assert.assertFalse(EDTFPattern.YEAR_MONTH_RANGE.matches("2010-02-13"));
    testDatable("2010-01-01:12", "2010-12-31:12", Datable.Certainty.MEDIUM, new Datable("2010-??"));
  }

  @Test
  public void convertDAY_MONTH_YEAR() {
    Assert.assertTrue(EDTFPattern.DAY_MONTH_YEAR.matches("06-03-1854"));
    Assert.assertTrue(EDTFPattern.DAY_MONTH_YEAR.matches("6-3-1854"));
    testDatable("1854-03-06:12", "1854-03-06:12", Datable.Certainty.HIGH, new Datable("06-03-1854"));
  }

  @Test
  public void convertMONTH_YEAR() {
    Assert.assertTrue(EDTFPattern.MONTH_YEAR_RX.matches("03-1854"));
    Assert.assertTrue(EDTFPattern.MONTH_YEAR_RX.matches("3-1854"));
    testDatable("1854-03-01:12", "1854-03-31:12", Datable.Certainty.MEDIUM, new Datable("03-1854"));
  }

  @Test
  public void convertYEAR_MONTH_DAY_H() {
    String edtf = "1648-05-15";
    Assert.assertTrue(EDTFPattern.YEAR_MONTH_DAY_H.matches(edtf));
    testDatable("1648-05-15:12", "1648-05-15:12", Datable.Certainty.HIGH, new Datable(edtf));
  }

  @Test
  public void convertYEAR_MONTH_DAY() {
    String edtf = "16480515";
    Assert.assertTrue(EDTFPattern.YEAR_MONTH_DAY.matches(edtf));
    testDatable("1648-05-15:12", "1648-05-15:12", Datable.Certainty.HIGH, new Datable(edtf));
  }

  @Test
  public void convertYEAR_MONTH_DAY_HQ() {
    String edtf = "1648-05-15?";
    Assert.assertTrue(EDTFPattern.YEAR_MONTH_DAY_HQ.matches(edtf));
    testDatable("1648-05-15:12", "1648-05-15:12", Datable.Certainty.LOW, new Datable(edtf));
  }

  @Test
  public void convertYEAR_MONTH_DAY_Q() {
    String edtf = "16480515?";
    Assert.assertTrue(EDTFPattern.YEAR_MONTH_DAY_Q.matches(edtf));
    testDatable("1648-05-15:12", "1648-05-15:12", Datable.Certainty.LOW, new Datable(edtf));
  }

  @Test
  public void convertYEAR_MONTH_DAY_HA() {
    String edtf = "1648-05-15~";
    Assert.assertTrue(EDTFPattern.YEAR_MONTH_DAY_HA.matches(edtf));
    testDatable("1648-05-15:12", "1648-05-15:12", Datable.Certainty.MEDIUM, new Datable(edtf));
  }

  @Test
  public void convertYEAR_MONTH_DAY_A() {
    String edtf = "16480515~";
    Assert.assertTrue(EDTFPattern.YEAR_MONTH_DAY_A.matches(edtf));
    testDatable("1648-05-15:12", "1648-05-15:12", Datable.Certainty.MEDIUM, new Datable(edtf));
  }

  @Test
  public void testYearConversion() throws ParseException {
    Datable datable = new Datable("1709/1710");
    Assert.assertEquals(1709, datable.getFromYear());
    Assert.assertEquals(1710, datable.getToYear());
  }

  /**
   * When a datable is initialized with a null value, it should throw an Exception.
   */
  @Test
  public void testNullString() {
    Datable datable = new Datable(null);
  }

}
