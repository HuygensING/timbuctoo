package nl.knaw.huygens.timbuctoo.rest.util.search;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.DomainEntityDTO;
import nl.knaw.huygens.timbuctoo.model.mapping.FieldNameMapFactory;
import nl.knaw.huygens.timbuctoo.model.mapping.FieldNameMap;
import nl.knaw.huygens.timbuctoo.model.mapping.MappingException;

import java.util.List;
import java.util.Map;

import static nl.knaw.huygens.timbuctoo.model.mapping.FieldNameMapFactory.Representation.CLIENT;
import static nl.knaw.huygens.timbuctoo.model.mapping.FieldNameMapFactory.Representation.INDEX;

public class DomainEntityDTOListFactory {
  private final FieldNameMapFactory fieldMapFactory;
  private final DomainEntityDTOFactory domainEntityDTOFactory;

  public DomainEntityDTOListFactory(){
    this(new FieldNameMapFactory(), new DomainEntityDTOFactory());
  }

  @Inject
  DomainEntityDTOListFactory(FieldNameMapFactory fieldMapFactory, DomainEntityDTOFactory domainEntityDTOFactory) {
    this.fieldMapFactory = fieldMapFactory;
    this.domainEntityDTOFactory = domainEntityDTOFactory;
  }

  public List<DomainEntityDTO> createFor(Class<? extends DomainEntity> type, List<Map<String, Object>> rawIndexData) throws SearchResultCreationException {
    FieldNameMap fieldNameMap = null;
    try {
      fieldNameMap = fieldMapFactory.create(INDEX, CLIENT, type);
    } catch (MappingException e) {
      throw new SearchResultCreationException(type, e);
    }
    List<DomainEntityDTO> dtos = Lists.newArrayList();
    for (Map<String, Object> dataRow : rawIndexData) {
      dtos.add(domainEntityDTOFactory.create(fieldNameMap, dataRow));
    }
    return dtos;
  }
}
