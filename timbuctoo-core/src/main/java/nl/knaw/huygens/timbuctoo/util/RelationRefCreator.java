package nl.knaw.huygens.timbuctoo.util;

import nl.knaw.huygens.timbuctoo.config.EntityMapper;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.RelationRef;
import nl.knaw.huygens.timbuctoo.storage.Storage;
import nl.knaw.huygens.timbuctoo.storage.StorageException;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class RelationRefCreator {
  private final TypeRegistry registry;
  private final Storage storage;

  @Inject
  public RelationRefCreator(TypeRegistry registry, Storage storage) {
    this.registry = registry;
    this.storage = storage;
  }

  // Relations are defined between primitive domain entities
  // Map to a domain entity in the package from which an entity is requested
  public RelationRef newRelationRef(EntityMapper mapper, Reference reference, String relationId, boolean accepted, int rev) throws StorageException {
    String iname = reference.getType();

    Class<? extends DomainEntity> type = registry.getDomainEntityType(iname);
    Class<? extends DomainEntity> mappedType = mapper.map(type);
    String mappedIName = TypeNames.getInternalName(mappedType);
    String xname = registry.getXNameForIName(mappedIName);
    DomainEntity entity = storage.getItem(mappedType, reference.getId());

    return new RelationRef(mappedIName, xname, reference.getId(), entity.getDisplayName(), relationId, accepted, rev);
  }

  public RelationRef newReadOnlyRelationRef(String type, String xType, String id, String displayName) {
    return new RelationRef(type, xType, id, displayName, null, true, 0);
  }

}
