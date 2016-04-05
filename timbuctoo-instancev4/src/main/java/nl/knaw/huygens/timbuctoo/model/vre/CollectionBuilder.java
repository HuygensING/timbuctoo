package nl.knaw.huygens.timbuctoo.model.vre;

import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.model.properties.ReadableProperty;
import nl.knaw.huygens.timbuctoo.model.properties.ReadWriteProperty;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class CollectionBuilder {
  private String entityTypeName;
  private String abstractType;
  private String collectionName;
  private final String defaultPrefix;
  private ReadableProperty displayName;
  private LinkedHashMap<String, ReadableProperty> properties = Maps.newLinkedHashMap();
  private final Map<String, Supplier<GraphTraversal<Object, Vertex>>> derivedRelations = Maps.newHashMap();
  private boolean isRelationCollection = false;

  private CollectionBuilder(String collectionName, String defaultPrefix) {
    this.collectionName = collectionName;
    this.defaultPrefix = defaultPrefix;
  }

  static CollectionBuilder timbuctooCollection(String collectionName, String defaultPrefix) {
    return new CollectionBuilder(collectionName, defaultPrefix);
  }

  public CollectionBuilder withEntityTypeName(String entityTypeName) {
    this.entityTypeName = entityTypeName;
    return this;
  }

  public CollectionBuilder withAbstractType(String abstractType) {
    this.abstractType = abstractType;
    return this;
  }

  public CollectionBuilder withDisplayName(ReadableProperty displayName) {
    this.displayName = displayName;
    return this;
  }

  public CollectionBuilder withProperty(String name, ReadWriteProperty property) {
    this.properties.put(name, property);
    return this;
  }

  public CollectionBuilder withDerivedRelation(String name, Supplier<GraphTraversal<Object, Vertex>> func) {
    this.derivedRelations.put(name, func);
    return this;
  }

  public CollectionBuilder isRelationCollection() {
    this.isRelationCollection = true;
    return this;
  }

  public void build(Vre vre) {
    if (entityTypeName == null) {
      if (collectionName.endsWith("s")) {
        entityTypeName = collectionName.substring(0, collectionName.length() - 1);
      } else {
        throw new RuntimeException("collection " + collectionName + "is not a plural (doesn't end in an 's') and no " +
          "entitytypename is provided");
      }
    }
    if (abstractType == null) {
      if (entityTypeName.startsWith(defaultPrefix)) {
        abstractType = entityTypeName.substring(defaultPrefix.length());
      } else {
        throw new RuntimeException("entityTypeName " + entityTypeName + "does not start with the default prefix (" +
          defaultPrefix + ") and no abstracttype is defined");
      }
    }

    Collection collection = new Collection(
      entityTypeName,
      abstractType,
      displayName,
      properties,
      collectionName,
      vre,
      derivedRelations,
      isRelationCollection);
    vre.addCollection(collection);
  }
}
