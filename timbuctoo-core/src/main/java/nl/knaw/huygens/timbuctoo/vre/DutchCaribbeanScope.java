package nl.knaw.huygens.timbuctoo.vre;

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public class DutchCaribbeanScope extends AbstractScope {

  private static final String DCAR_PACKAGE = "timbuctoo.model.dcar";

  public DutchCaribbeanScope() throws IOException {
    super(DCAR_PACKAGE);
  }

  @Override
  public String getId() {
    return "dcar";
  }

  @Override
  public String getName() {
    return "Dutch Caribbean Scope";
  }

  @Override
  public <T extends DomainEntity> boolean inScope(Class<T> type, String id) {
    return true;
  }

  @Override
  public <T extends DomainEntity> boolean inScope(T entity) {
    return getAllEntityTypes().contains(entity.getClass());
  }

}
