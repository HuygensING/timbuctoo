package nl.knaw.huygens.timbuctoo.vre;

import java.io.IOException;

public class FullVRE implements VRE {

  private final Scope scope;

  public FullVRE() throws IOException {
    scope = new FullScope();
  }

  @Override
  public String getName() {
    return "Timbuctoo";
  }

  @Override
  public Scope getScope() {
    return scope;
  }

}
