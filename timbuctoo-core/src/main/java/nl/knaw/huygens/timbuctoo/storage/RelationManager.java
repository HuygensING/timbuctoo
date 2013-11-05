package nl.knaw.huygens.timbuctoo.storage;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.util.CSVImporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class RelationManager {

  private static final Logger LOG = LoggerFactory.getLogger(RelationManager.class);

  private final TypeRegistry registry;
  private final StorageManager storageManager;

  @Inject
  public RelationManager(TypeRegistry registry, StorageManager storageManager) {
    this.registry = registry;
    this.storageManager = storageManager;
  }

  /**
   * Reads {@code RelationType} definitions from the specified file
   * which must be present on the classpath.
   */
  public void importRelationTypes(String fileName) {
    try {
      InputStream stream = RelationManager.class.getClassLoader().getResourceAsStream(fileName);
      RelationTypeImporter importer = new RelationTypeImporter();
      importer.handleFile(stream, 6, false);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void addRelationType(String regularName, String inverseName, Class<? extends DomainEntity> sourceType, Class<? extends DomainEntity> targetType, boolean reflexive, boolean symmetric) {
    RelationType type = new RelationType(regularName, inverseName, sourceType, targetType);
    type.setReflexive(reflexive);
    type.setSymmetric(symmetric);
    try {
      storageManager.addEntity(RelationType.class, type);
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
    checkArgument(reference.getType().equals("relationtype"), "got type %s", reference.getType());
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
        if (storageManager.relationExists(relation)) {
          LOG.info("Ignored duplicate {}", relation.getDisplayName());
        } else {
          return storageManager.addEntity(Relation.class, relation);
        }
      } catch (IOException e) {
        LOG.error("Failed to add {}; {}", relation.getDisplayName(), e.getMessage());
      }
    }
    return null;
  }

  // -------------------------------------------------------------------

  private class RelationTypeImporter extends CSVImporter {

    public RelationTypeImporter() {
      super(new PrintWriter(System.err), ';', '"', 4);
    }

    @Override
    protected void handleLine(String[] items) {
      String regularName = items[0];
      String inverseName = items[1];
      Class<? extends DomainEntity> sourceType = convert(items[2]);
      Class<? extends DomainEntity> targetType = convert(items[3]);
      boolean reflexive = Boolean.parseBoolean(items[4]);
      boolean symmetric = Boolean.parseBoolean(items[5]);
      if (getRelationTypeByName(regularName) != null) {
        LOG.info("Relation type '{}' already exists", regularName);
      } else {
        addRelationType(regularName, inverseName, sourceType, targetType, reflexive, symmetric);
      }
    }

    private Class<? extends DomainEntity> convert(String typeName) {
      String iname = typeName.toLowerCase();
      if (iname.equals("domainentity")) {
        return DomainEntity.class;
      } else {
        @SuppressWarnings("unchecked")
        Class<? extends DomainEntity> type = (Class<? extends DomainEntity>) registry.getTypeForIName(iname);
        checkState(type != null, "'%s' is not a domain entity", typeName);
        return type;
      }
    }
  }

  // -------------------------------------------------------------------

  public RelationBuilder getBuilder() {
    return new RelationBuilder();
  }

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
