package nl.knaw.huygens.timbuctoo.index;

import static nl.knaw.huygens.timbuctoo.config.TypeRegistry.toBaseDomainEntity;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.vre.VRE;

public class IndexNameCreator {

  public String getIndexNameFor(VRE vre, Class<? extends DomainEntity> type) {
    return String.format("%s.%s", vre.getScopeId(), TypeNames.getInternalName(toBaseDomainEntity(type)));
  }
}
