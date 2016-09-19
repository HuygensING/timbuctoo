package nl.knaw.huygens.timbuctoo.remote.rs.xml;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class CapabilityTest {

  @Test
  public void forString() {
    for (Capability capa : Capability.values()) {
      assertThat(capa, equalTo(Capability.forString(capa.xmlValue)));
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void forWrongString() {
    Capability.forString("bar");
  }
}
