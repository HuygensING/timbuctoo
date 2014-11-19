package nl.knaw.huygens.timbuctoo.tools.oaipmh;

import static nl.knaw.huygens.timbuctoo.tools.oaipmh.CollectionUtils.getPluralOfBaseCollection;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public class DublinCoreIdentifierCreator {

  public String create(DomainEntity domainEntity, String baseURL) {
    String id = domainEntity.getId();
    String baseCollection = getPluralOfBaseCollection(domainEntity);
    return String.format("%s/%s/%s", baseURL, baseCollection, id);
  }
}
