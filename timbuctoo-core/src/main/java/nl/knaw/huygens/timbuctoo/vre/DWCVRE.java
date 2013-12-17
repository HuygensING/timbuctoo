package nl.knaw.huygens.timbuctoo.vre;

import java.io.IOException;

public class DWCVRE implements VRE {
  private final Scope scope;

  public DWCVRE() throws IOException {
    scope = new DWCScope();
  }

  @Override
  public String getName() {
    return "DWC";
  }

  @Override
  public Scope getScope() {
    return scope;
  }

}
