package nl.knaw.huygens.timbuctoo.rest.resources;

import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.EntityRef;

import com.google.common.collect.Lists;

public class EntityRefCreator {

  public <T extends DomainEntity> List<EntityRef> createRefs(Class<T> type, List<T> result) {
    String itype = TypeNames.getInternalName(type);
    String xtype = TypeNames.getExternalName(type);
    List<EntityRef> list = Lists.newArrayListWithCapacity(result.size());
    for (DomainEntity entity : result) {
      list.add(new EntityRef(itype, xtype, entity.getId(), entity.getDisplayName()));
    }
    return list;
  }

}
