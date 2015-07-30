package nl.knaw.huygens.timbuctoo.rest.util.search;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.DomainEntityDTO;
import nl.knaw.huygens.timbuctoo.model.mapping.DomainEntityFieldNameMapFactory;
import nl.knaw.huygens.timbuctoo.model.mapping.FieldNameMap;

import java.util.List;
import java.util.Map;

import static nl.knaw.huygens.timbuctoo.model.mapping.DomainEntityFieldNameMapFactory.Representation.CLIENT;
import static nl.knaw.huygens.timbuctoo.model.mapping.DomainEntityFieldNameMapFactory.Representation.INDEX;

public class DomainEntityDTOListFactory {
  private final DomainEntityFieldNameMapFactory fieldMapFactory;
  private final DomainEntityDTOFactory domainEntityDTOFactory;

  public DomainEntityDTOListFactory(){
    this(new DomainEntityFieldNameMapFactory(), new DomainEntityDTOFactory());
  }

  @Inject
  DomainEntityDTOListFactory(DomainEntityFieldNameMapFactory fieldMapFactory, DomainEntityDTOFactory domainEntityDTOFactory) {
    this.fieldMapFactory = fieldMapFactory;
    this.domainEntityDTOFactory = domainEntityDTOFactory;
  }

  public List<DomainEntityDTO> createFor(Class<? extends DomainEntity> type, List<Map<String, Object>> rawIndexData) {
    FieldNameMap fieldNameMap = fieldMapFactory.create(INDEX, CLIENT, type);
    List<DomainEntityDTO> dtos = Lists.newArrayList();
    for (Map<String, Object> dataRow : rawIndexData) {
      dtos.add(domainEntityDTOFactory.create(fieldNameMap, dataRow));
    }
    return dtos;
  }
}
