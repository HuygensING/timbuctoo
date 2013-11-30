package nl.knaw.huygens.timbuctoo.storage;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class RelationManager {

  static final Logger LOG = LoggerFactory.getLogger(RelationManager.class);

  final TypeRegistry registry;
  private final StorageManager storageManager;

  @Inject
  public RelationManager(TypeRegistry registry, StorageManager storageManager) {
    this.registry = registry;
    this.storageManager = storageManager;
  }

  public void addRelationType(String regularName, String inverseName, Class<? extends DomainEntity> sourceType, Class<? extends DomainEntity> targetType, boolean reflexive, boolean symmetric) {
    RelationType type = new RelationType(regularName, inverseName, sourceType, targetType);
    type.setReflexive(reflexive);
    type.setSymmetric(symmetric);
    try {
      storageManager.addSystemEntity(RelationType.class, type);
    } catch (IOException e) {
      LOG.error("Failed to add {}; {}", type.getDisplayName(), e.getMessage());
    }
  }

  private static final String REGULAR_NAME = FieldMapper.propertyName(RelationType.class, "regularName");

  /**
   * Returns the relation type with the specified name,
   * or {@code null} if it does not exist.
   */
  public RelationType getRelationTypeByName(String name) {
    return storageManager.findEntity(RelationType.class, REGULAR_NAME, name);
  }

  /**
   * Returns the relation type with the specified id,
   * or {@code null} if it does not exist.
   */
  public RelationType getRelationTypeById(String id) {
    return storageManager.getEntity(RelationType.class, id);
  }

  /**
   * Returns the relation type with the specified reference,
   * or {@code null} if it does not exist.
   */
  public RelationType getRelationType(Reference reference) {
    checkArgument(reference.refersToType(RelationType.class), "got type %s", reference.getType());
    return getRelationTypeById(reference.getId());
  }

  public <T extends Relation> String storeRelation(Class<T> type, Reference sourceRef, Reference relTypeRef, Reference targetRef) {
    RelationBuilder<T> builder = getBuilder(type);

    RelationType relationType = getRelationType(relTypeRef);
    builder.type(relTypeRef);
    /* 
     * If the relationType is symmetric order the relation on id.
     * This way we can be sure the relation is saved once.  
     */
    if (relationType.isSymmetric() && sourceRef.getId().compareTo(targetRef.getId()) > 0) {
      builder.source(targetRef).target(sourceRef);
    } else {
      builder.source(sourceRef).target(targetRef);
    }
    T relation = builder.build();
    if (relation != null) {
      try {
        if (storageManager.relationExists(relation)) {
          LOG.info("Ignored duplicate {}", relation.getDisplayName());
        } else {
          return storageManager.addDomainEntity(type, relation);
        }
      } catch (IOException e) {
        LOG.error("Failed to add {}; {}", relation.getDisplayName(), e.getMessage());
      }
    }
    return null;
  }

  // -------------------------------------------------------------------

  public <T extends Relation> RelationBuilder<T> getBuilder(Class<T> type) {
    checkArgument(type != null && type.getSuperclass() == Relation.class);
    return new RelationBuilder<T>(type);
  }

  public class RelationBuilder<T extends Relation> {
    private T relation;

    public RelationBuilder(Class<T> type) {
      try {
        relation = type.newInstance();
      } catch (Exception e) {
        throw new RuntimeException("Failed to create instance of " + type);
      }
    }

    public RelationBuilder<T> type(Reference ref) {
      relation.setTypeRef(ref);
      return this;
    }

    public RelationBuilder<T> source(Reference ref) {
      relation.setSourceRef(ref);
      return this;
    }

    public RelationBuilder<T> source(Class<? extends Entity> typeToken, String id) {
      return source(new Reference(typeToken, id));
    }

    public RelationBuilder<T> target(Reference ref) {
      relation.setTargetRef(ref);
      return this;
    }

    public RelationBuilder<T> target(Class<? extends Entity> typeToken, String id) {
      return target(new Reference(typeToken, id));
    }

    public RelationBuilder<T> accept(boolean accepted) {
      relation.setAccepted(accepted);
      return this;
    }

    public T build() {
      if (relation.getTypeRef() == null) {
        LOG.error("Missing relation type ref");
        return null;
      }
      if (relation.getSourceType() == null || relation.getSourceId() == null) {
        LOG.error("Missing source ref");
        return null;
      }
      if (relation.getTargetType() == null || relation.getTargetId() == null) {
        LOG.error("Missing target ref");
        return null;
      }
      RelationType relationType = storageManager.getEntity(RelationType.class, relation.getTypeRef().getId());
      if (relationType == null) {
        LOG.error("Unknown relation type {}", relation.getTypeRef().getId());
        return null;
      }
      String iname = relation.getSourceType();
      Class<? extends Entity> actualType = registry.getTypeForIName(iname);
      if (!relationType.getSourceDocType().isAssignableFrom(actualType)) {
        LOG.error("Incompatible source type {}", iname);
        return null;
      }
      iname = relation.getTargetRef().getType();
      actualType = registry.getTypeForIName(iname);
      if (!relationType.getTargetDocType().isAssignableFrom(actualType)) {
        LOG.error("Incompatible target type {}", iname);
        return null;
      }
      return relation;
    }
  }

}
