package nl.knaw.huygens.timbuctoo.rest.resources;

import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.ClientEntityRepresentation;

import com.google.common.collect.Lists;

public class ClientEntityRepresentationCreator {

  public <T extends DomainEntity> List<ClientEntityRepresentation> createRefs(Class<T> type, List<T> result) {
    String itype = TypeNames.getInternalName(type);
    String xtype = TypeNames.getExternalName(type);
    List<ClientEntityRepresentation> list = Lists.newArrayListWithCapacity(result.size());
    for (DomainEntity entity : result) {
      list.add(new ClientEntityRepresentation(itype, xtype, entity.getId(), entity.getDisplayName()));
    }
    return list;
  }

}
