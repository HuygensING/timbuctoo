package nl.knaw.huygens.timbuctoo.storage;

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.config.DocTypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.atlg.ATLGArchive;
import nl.knaw.huygens.timbuctoo.model.atlg.ATLGArchiver;
import nl.knaw.huygens.timbuctoo.model.atlg.ATLGKeyword;
import nl.knaw.huygens.timbuctoo.model.atlg.ATLGPerson;

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

  public void importRelationTypes() {
    addRelationType("is_creator_of", "is_created_by", ATLGArchiver.class, ATLGArchive.class, false);
    addRelationType("has_keyword", "is_keyword_of", DomainEntity.class, ATLGKeyword.class, false);
    addRelationType("has_person", "is_person_of", DomainEntity.class, ATLGPerson.class, false);
    addRelationType("has_place", "is_place_of", DomainEntity.class, ATLGKeyword.class, false);
    addRelationType("has_parent_archive", "has_child_archive", ATLGArchive.class, ATLGArchive.class, false);
    addRelationType("has_sibling_archive", "has_sibling_archive", ATLGArchive.class, ATLGArchive.class, true);
    addRelationType("has_sibling_archiver", "has_sibling_archiver", ATLGArchiver.class, ATLGArchiver.class, true);
  }

  private void addRelationType(String regularName, String inverseName, Class<? extends DomainEntity> sourceType, Class<? extends DomainEntity> targetType, boolean symmetric) {
    RelationType type = new RelationType(regularName, inverseName, sourceType, targetType);
    type.setReflexive(false);
    type.setSymmetric(symmetric);
    try {
      storageManager.addEntityWithoutPersisting(RelationType.class, type, false); // don't index
    } catch (IOException e) {
      LOG.error("Failed to add {}; {}", type.getDisplayName(), e.getMessage());
    }
  }

  /**
   * Returns the relation type with the specified name,
   * or {@code null} if it does not exist.
   */
  public RelationType getRelationTypeByName(String name) {
    return storageManager.findEntity(RelationType.class, "regularName", name);
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
    Preconditions.checkArgument(reference.getType().equals("relationtype"), "got type %s", reference.getType());
    return getRelationTypeById(reference.getId());
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
