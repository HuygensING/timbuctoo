package nl.knaw.huygens.timbuctoo.tools.oaipmh;

import static nl.knaw.huygens.timbuctoo.tools.oaipmh.CollectionUtils.getPluralOfBaseCollection;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public class CMDICollectionNameCreator {

  public String create(DomainEntity domainEntity, String vreName) {

    return String.format("%s %s", vreName, getPluralOfBaseCollection(domainEntity));
  }

}
