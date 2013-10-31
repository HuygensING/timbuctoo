package nl.knaw.huygens.timbuctoo.vre;

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

/**
 * A {@code Scope} with all primitive domain entities.
 */
public class BaseScope extends AbstractScope {

  public BaseScope() throws IOException {
    super("timbuctoo.model");
  }

  @Override
  public String getId() {
    return "base";
  }

  @Override
  public String getName() {
    return "Base Scope";
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
