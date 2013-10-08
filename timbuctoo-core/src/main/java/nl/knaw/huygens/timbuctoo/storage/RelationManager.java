package nl.knaw.huygens.timbuctoo.storage;

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.config.DocTypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class RelationManager {

  private static final Logger LOG = LoggerFactory.getLogger(RelationManager.class);

  private final DocTypeRegistry registry;
  private final StorageManager storageManager;

  @Inject
  public RelationManager(DocTypeRegistry registry, StorageManager storageManager) {
    this.registry = registry;
    this.storageManager = storageManager;
  }

  /**
   * Returns the relation type with the specified id,
   * or null if it does not exist.
   */
  public RelationType getRelationType(String id) {
    return storageManager.getEntity(RelationType.class, id);
  }

  /**
   * Returns the relation type with the specified reference,
   * or null if it does not exist.
   */
  public RelationType getRelationType(Reference reference) {
    Preconditions.checkArgument(reference.getType().equals("relationtype"), "got type %s", reference.getType());
    return getRelationType(reference.getId());
  }

  public String storeRelation(Reference sourceRef, Reference relTypeRef, Reference targetRef) {
    RelationType relationType = getRelationType(relTypeRef);
    RelationBuilder builder = getBuilder().type(relTypeRef);
    if (relationType.isSymmetric() && sourceRef.getId().compareTo(targetRef.getId()) > 0) {
      builder.source(targetRef).target(sourceRef);
    } else {
      builder.source(sourceRef).target(targetRef);
    }
    Relation relation = builder.build();
    if (relation != null) {
      try {
        if (storageManager.countRelations(relation) > 0) {
          LOG.info("Ignored duplicate {}", relation.getDisplayName());
        } else {
          return storageManager.addEntityWithoutPersisting(Relation.class, relation, true);
        }
      } catch (IOException e) {
        LOG.error("Failed to add {}; {}", relation.getDisplayName(), e.getMessage());
      }
    }
    return null;
  }

  public RelationBuilder getBuilder() {
    return new RelationBuilder();
  }

  // -------------------------------------------------------------------

  public class RelationBuilder {
    private Relation relation;

    public RelationBuilder() {
      relation = new Relation();
    }

    public RelationBuilder type(Reference ref) {
      relation.setTypeRef(ref);
      return this;
    }

    public RelationBuilder source(Reference ref) {
      relation.setSourceRef(ref);
      return this;
    }

    public RelationBuilder source(Class<? extends Entity> typeToken, String id) {
      return source(new Reference(typeToken, id));
    }

    public RelationBuilder target(Reference ref) {
      relation.setTargetRef(ref);
      return this;
    }

    public RelationBuilder target(Class<? extends Entity> typeToken, String id) {
      return target(new Reference(typeToken, id));
    }

    public RelationBuilder accept(boolean accepted) {
      relation.setAccepted(accepted);
      return this;
    }

    public Relation build() {
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
