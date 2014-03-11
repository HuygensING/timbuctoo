package nl.knaw.huygens.timbuctoo.index;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.vre.Scope;

public class IndexNameCreator {

  private final TypeRegistry registry;

  public IndexNameCreator(TypeRegistry registry) {
    this.registry = registry;
  }

  public String getIndexNameFor(Scope scope, Class<? extends DomainEntity> type) {
    return String.format("%s.%s", scope.getId(), registry.getINameForType(type));
  }
}
