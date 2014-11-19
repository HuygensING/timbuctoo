package nl.knaw.huygens.timbuctoo.tools.oaipmh;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public class CollectionUtils {
  public static String getSingularNameOfBaseCollection(DomainEntity domainEntity) {
    Class<? extends DomainEntity> baseType = TypeRegistry.toBaseDomainEntity(domainEntity.getClass());
    return baseType.getSimpleName().toLowerCase();
  }

  public static String getPluralOfBaseCollection(DomainEntity domainEntity) {
    return String.format("%ss", getSingularNameOfBaseCollection(domainEntity));
  }
}
