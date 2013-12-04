package nl.knaw.huygens.timbuctoo.vre;

import java.io.IOException;

public class TestVRE implements VRE {

  private final Scope scope;

  public TestVRE() throws IOException {
    scope = new TestScope();
  }

  @Override
  public String getName() {
    return "TestVRE";
  }

  @Override
  public Scope getScope() {
    return scope;
  }
}
