package nl.knaw.huygens.timbuctoo.vre;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public class NotInScopeException extends Exception {
  public NotInScopeException(Class<? extends DomainEntity> type, String vreId){
    super(String.format("%s is not in the scope of %s", TypeNames.getExternalName(type), vreId));
  }
}
