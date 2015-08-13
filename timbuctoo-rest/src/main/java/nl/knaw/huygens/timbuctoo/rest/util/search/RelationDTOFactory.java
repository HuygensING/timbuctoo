package nl.knaw.huygens.timbuctoo.rest.util.search;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.DomainEntityDTO;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationDTO;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.mapping.FieldNameMap;
import nl.knaw.huygens.timbuctoo.model.mapping.FieldNameMapFactory;
import nl.knaw.huygens.timbuctoo.model.mapping.MappingException;
import nl.knaw.huygens.timbuctoo.vre.NotInScopeException;
import nl.knaw.huygens.timbuctoo.vre.SearchException;
import nl.knaw.huygens.timbuctoo.vre.VRE;

import java.util.List;
import java.util.Map;

public class RelationDTOFactory {
  private final Repository repository;
  private final TypeRegistry typeRegistry;
  private final DomainEntityDTOFactory domainEntityDTOFactory;
  private final FieldNameMapFactory fieldNameMapFactory;

  @Inject
  public RelationDTOFactory(Repository repository, TypeRegistry typeRegistry, DomainEntityDTOFactory domainEntityDTOFactory, FieldNameMapFactory fieldNameMapFactory) {
    this.repository = repository;
    this.typeRegistry = typeRegistry;
    this.domainEntityDTOFactory = domainEntityDTOFactory;
    this.fieldNameMapFactory = fieldNameMapFactory;
  }

  public RelationDTO create(VRE vre, Class<? extends DomainEntity> type, Map<String, Object> dataRow) throws NotInScopeException, SearchException, MappingException {
    RelationDTO dto = new RelationDTO();

    dto.setType(TypeNames.getInternalName(type));
    String id = getStringValue(dataRow.get(Entity.INDEX_FIELD_ID));
    dto.setId(id);
    dto.createPath(TypeNames.getExternalName(type), id);

    String relTypeId = getStringValue(dataRow.get(Relation.TYPE_ID_FACET_NAME));
    RelationType relationType = repository.getRelationTypeById(relTypeId, true);
    dto.setRelationName(relationType.getRegularName());


    DomainEntityDTO sourceDTO = createEntityDTO(vre, dataRow, Relation.SOURCE_ID_FACET_NAME, Relation.INDEX_FIELD_SOURCE_TYPE);
    dto.setSourceName(sourceDTO.getDisplayName());
    dto.setSourceData(sourceDTO.getData());

    DomainEntityDTO targetDTO = createEntityDTO(vre, dataRow, Relation.TARGET_ID_FACET_NAME, Relation.INDEX_FIELD_TARGET_TYPE);
    dto.setTargetName(targetDTO.getDisplayName());
    dto.setTargetData(targetDTO.getData());

    return dto;
  }

  private String getStringValue(Object obj) {
    if (Iterable.class.isAssignableFrom(obj.getClass())) {
      Iterable<?> iterable = (Iterable<?>) obj;
      for (Object o : iterable) {
        return String.valueOf(o);
      }

    }
    return String.valueOf(obj);
  }

  private DomainEntityDTO createEntityDTO(VRE vre, Map<String, Object> dataRow, String idField, String typeField) throws NotInScopeException, SearchException, MappingException {
    String id = getStringValue(dataRow.get(idField));
    String typeString = getStringValue(dataRow.get(typeField));

    // relations contain only base types
    Class<? extends DomainEntity> baseType = typeRegistry.getDomainEntityType(typeString);
    Class<? extends DomainEntity> type = vre.mapToScopeType(baseType);
    List<Map<String, Object>> data = vre.getRawDataFor(type, Lists.newArrayList(id));
    FieldNameMap fieldNameMap = fieldNameMapFactory.create(FieldNameMapFactory.Representation.INDEX, FieldNameMapFactory.Representation.CLIENT, type);

    return domainEntityDTOFactory.create(type, fieldNameMap, data.get(0));
  }
}
