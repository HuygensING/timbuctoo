package nl.knaw.huygens.repository.tools.util;

import nl.knaw.huygens.repository.tools.util.EncodingFixer;

import org.junit.Assert;
import org.junit.Test;

public class EncodingFixerTest {

  @Test
  public void testConversion() {
    Assert.assertEquals("Belvédère", EncodingFixer.convert1("BelvÃ©dÃ¨re"));
    Assert.assertEquals("Curaçao", EncodingFixer.convert1("CuraÃ§ao"));
    Assert.assertEquals("Wolffenbüttel", EncodingFixer.convert1("WolffenbÃ¼ttel"));
    Assert.assertEquals("notariële", EncodingFixer.convert1("notariÃ«le"));
  }

  @Test
  public void testInvariance() {
    Assert.assertEquals("Belvédère", EncodingFixer.convert1("Belvédère"));
    Assert.assertEquals("Curaçao", EncodingFixer.convert1("Curaçao"));
  }

}
