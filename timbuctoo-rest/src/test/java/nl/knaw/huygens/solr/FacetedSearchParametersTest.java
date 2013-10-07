package nl.knaw.huygens.solr;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class FacetedSearchParametersTest {

  private FacetedSearchParameters instance;

  @Before
  public void setUp() {
    instance = new FacetedSearchParameters();
  }

  @Test
  public void testSetTermWithNonEmptyString() {
    testSetTerm("test", "test");
  }

  @Test
  public void testSetTermWithEmptyString() {
    testSetTerm("", "*");
  }

  @Test
  public void testSetFilledTermWithEmptyString() {
    instance.setTerm("test");
    testSetTerm("", "*");
  }

  @Test
  public void testSetFilledTermWithNonEmptyString() {
    instance.setTerm("test1");
    testSetTerm("test", "test");
  }

  private void testSetTerm(String newValue, String expectedValue) {
    instance.setTerm(newValue);
    assertEquals(expectedValue, instance.getTerm());
  }
}
