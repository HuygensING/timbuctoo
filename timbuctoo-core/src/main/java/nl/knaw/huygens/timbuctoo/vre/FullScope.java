package nl.knaw.huygens.timbuctoo.vre;

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

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

  @Override
  public <T extends DomainEntity> boolean inScope(Class<T> type, String id) {
    return true;
  }

  @Override
  public <T extends DomainEntity> boolean inScope(Class<T> type, T entity) {
    return true;
  }

}
