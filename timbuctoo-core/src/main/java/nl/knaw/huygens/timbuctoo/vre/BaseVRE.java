package nl.knaw.huygens.timbuctoo.vre;

import java.io.IOException;

public class BaseVRE implements VRE {

  private final Scope scope;

  public BaseVRE() throws IOException {
    scope = new BaseScope();
  }

  @Override
  public String getName() {
    return "BaseVRE";
  }

  @Override
  public Scope getScope() {
    return scope;
  }

}
