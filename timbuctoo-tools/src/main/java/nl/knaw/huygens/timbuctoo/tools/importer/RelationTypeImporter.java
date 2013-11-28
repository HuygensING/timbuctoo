package nl.knaw.huygens.timbuctoo.tools.importer;

import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.RelationManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to import all the known relation types.
 */
public class RelationTypeImporter extends CSVImporter {

  private final static Logger LOG = LoggerFactory.getLogger(RelationTypeImporter.class);
  private final RelationManager relationManager;
  private final TypeRegistry registry;

  public RelationTypeImporter(RelationManager relationManager, TypeRegistry registry) {
    super(new PrintWriter(System.err), ';', '"', 4);
    this.relationManager = relationManager;
    this.registry = registry;
  }

  /**
   * Reads {@code RelationType} definitions from the specified file which must be present on the classpath.
   * Convenience method for importing a file, that uses {@code handleLine}.
   */
  public void importRelationTypes(String fileName) {
    try {
      InputStream stream = RelationManager.class.getClassLoader().getResourceAsStream(fileName);
      this.handleFile(stream, 6, false);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void handleLine(String[] items) {
    String regularName = items[0];
    String inverseName = items[1];
    Class<? extends DomainEntity> sourceType = convert(items[2]);
    Class<? extends DomainEntity> targetType = convert(items[3]);
    boolean reflexive = Boolean.parseBoolean(items[4]);
    boolean symmetric = Boolean.parseBoolean(items[5]);
    if (this.relationManager.getRelationTypeByName(regularName) != null) {
      LOG.info("Relation type '{}' already exists", regularName);
    } else {
      this.relationManager.addRelationType(regularName, inverseName, sourceType, targetType, reflexive, symmetric);
    }
  }

  private Class<? extends DomainEntity> convert(String typeName) {
    String iname = typeName.toLowerCase();
    if (iname.equals("domainentity")) {
      return DomainEntity.class;
    } else {
      @SuppressWarnings("unchecked")
      Class<? extends DomainEntity> type = (Class<? extends DomainEntity>) this.registry.getTypeForIName(iname);
      checkState(type != null, "'%s' is not a domain entity", typeName);
      return type;
    }
  }
}