package nl.knaw.huygens.repository.util;

import org.junit.Assert;
import org.junit.Test;

public class EncodingFixerTest {

  @Test
  public void testConversion() {
    Assert.assertEquals("Curaçao", EncodingFixer.convert1("CuraÃ§ao"));
    Assert.assertEquals("Één", EncodingFixer.convert1("Ã‰Ã©n"));
  }

  @Test
  public void testInvariance() {
    Assert.assertEquals("Curaçao", EncodingFixer.convert1("Curaçao"));
    Assert.assertEquals("Één", EncodingFixer.convert1("Één"));
  }

}
