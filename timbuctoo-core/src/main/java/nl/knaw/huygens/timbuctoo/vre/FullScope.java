package nl.knaw.huygens.timbuctoo.vre;

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public class FullScope extends AbstractScope {

  public FullScope() throws IOException {
    // primitive entity types
    addPackage("timbuctoo.model");
    fixBaseTypes();
    // additional entity types
    addPackage("timbuctoo.model.atlg");
    addPackage("timbuctoo.model.ckcc");
    addPackage("timbuctoo.model.dcar");
    addPackage("timbuctoo.model.dwcbia");
    addPackage("timbuctoo.model.raa");
    fixAllTypes();
  }

  @Override
  public String getName() {
    return "FullScope";
  }

  @Override
  public <T extends DomainEntity> boolean inScope(Class<T> type, String id) {
    return true;
  }

}
