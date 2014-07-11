package nl.knaw.huygens.timbuctoo.rest.resources;

import java.util.List;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.EntityMapper;
import nl.knaw.huygens.timbuctoo.config.EntityMappers;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.ClientRelationRepresentation;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class ClientRelationRepresentationCreator {
  private static final Logger LOG = LoggerFactory.getLogger(ClientRelationRepresentationCreator.class);

  private final Repository repository;
  private final TypeRegistry registry;

  @Inject
  public ClientRelationRepresentationCreator(Repository repository, TypeRegistry registry) {
    this.repository = repository;
    this.registry = registry;

  }

  @SuppressWarnings("unchecked")
  public <T extends DomainEntity> List<ClientRelationRepresentation> createRefs(Class<T> type, List<T> result) {

    if (!Relation.class.isAssignableFrom(type)) {
      throw new RuntimeException("Type " + type + " is not a Relation");
    }

    return createRelationRefs((Class<? extends Relation>) type, (List<Relation>) result);
  }

  private List<ClientRelationRepresentation> createRelationRefs(Class<? extends Relation> type, List<Relation> relations) {
    EntityMappers entityMappers = new EntityMappers(registry.getDomainEntityTypes());
    EntityMapper mapper = entityMappers.getEntityMapper(type);

    String itype = TypeNames.getInternalName(type);
    String xtype = TypeNames.getExternalName(type);
    List<ClientRelationRepresentation> list = Lists.newArrayListWithCapacity(relations.size());
    for (Relation relation : relations) {
      RelationType relationType = repository.getRelationTypeById(relation.getTypeId());
      String relationName = relationType.getRegularName();
      DomainEntity source = retrieveEntity(mapper, relation.getSourceType(), relation.getSourceId());
      String sourceName = (source != null) ? source.getDisplayName() : "[unknown]";
      DomainEntity target = retrieveEntity(mapper, relation.getTargetType(), relation.getTargetId());
      String targetName = (target != null) ? target.getDisplayName() : "[unknown]";
      list.add(new ClientRelationRepresentation(itype, xtype, relation.getId(), relationName, sourceName, targetName));
    }
    return list;
  }

  private DomainEntity retrieveEntity(EntityMapper mapper, String typeName, String typeId) {
    Class<? extends DomainEntity> type = registry.getDomainEntityType(typeName);
    if (type == null) {
      LOG.error("Failed to convert {} to a domain entity", typeName);
      return null;
    }
    Class<? extends DomainEntity> mappedType = (mapper != null) ? mapper.map(type) : type;
    return repository.getEntity(mappedType, typeId);
  }

}
