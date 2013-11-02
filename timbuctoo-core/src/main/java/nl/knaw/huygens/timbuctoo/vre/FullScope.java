package nl.knaw.huygens.timbuctoo.vre;

import java.io.IOException;

public class FullScope extends AbstractScope {

  public FullScope() throws IOException {
    addPackage("timbuctoo.model");
    addPackage("timbuctoo.model.atlg");
    addPackage("timbuctoo.model.ckcc");
    addPackage("timbuctoo.model.dcar");
    addPackage("timbuctoo.model.dwcbia");
    addPackage("timbuctoo.model.raa");
    buildTypes();
  }

  @Override
  public String getId() {
    return "full";
  }

  @Override
  public String getName() {
    return "Full Scope";
  }

}
