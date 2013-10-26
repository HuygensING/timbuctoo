package nl.knaw.huygens.timbuctoo.vre;

import java.io.IOException;

public class DutchCaribbeanVRE implements VRE {

  private final Scope scope;

  public DutchCaribbeanVRE() throws IOException {
    scope = new DutchCaribbeanScope();
  }

  @Override
  public String getName() {
    return "DutchCaribbean";
  }

  @Override
  public Scope getScope() {
    return scope;
  }

}
