package nl.knaw.huygens.timbuctoo.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import nl.knaw.huygens.timbuctoo.model.util.FloruitPeriod;

import org.junit.Test;

public class FloruitPeriodTest {
  @Test
  public void testToStringForOneDate() {
    String expected = "fl. 1012";

    FloruitPeriod period = new FloruitPeriod("1012");

    assertThat(period.toString(), is(equalTo(expected)));
  }

  @Test
  public void testToStringForTwoDatesSingleYear() {
    String expected = "fl. 1012";

    FloruitPeriod period = new FloruitPeriod("10121010", "10121212");

    assertThat(period.toString(), is(equalTo(expected)));
  }

  @Test
  public void testToStringForMultipleYears() {
    String expected = "fl. 1012 - 1014";

    FloruitPeriod period = new FloruitPeriod("10121010", "10141212");

    assertThat(period.toString(), is(equalTo(expected)));
  }
}
