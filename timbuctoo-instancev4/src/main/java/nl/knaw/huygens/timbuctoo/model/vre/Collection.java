package nl.knaw.huygens.timbuctoo.model.vre;

import nl.knaw.huygens.timbuctoo.model.properties.LocalProperty;
import nl.knaw.huygens.timbuctoo.model.properties.ReadableProperty;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import javax.validation.constraints.NotNull;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toMap;

public class Collection {
  private final String entityTypeName;
  private final String collectionName;
  private final Vre vre;
  private final String abstractType;
  private final ReadableProperty displayName;
  private final LinkedHashMap<String, ReadableProperty> properties;
  private final LinkedHashMap<String, LocalProperty> writeableProperties;
  private final Map<String, Supplier<GraphTraversal<Object, Vertex>>> derivedRelations;
  private final boolean isRelationCollection;

  Collection(@NotNull String entityTypeName, @NotNull String abstractType,
             @NotNull ReadableProperty displayName, @NotNull LinkedHashMap<String, ReadableProperty> properties,
             @NotNull String collectionName, @NotNull Vre vre,
             @NotNull Map<String, Supplier<GraphTraversal<Object, Vertex>>> derivedRelations,
             boolean isRelationCollection) {
    this.entityTypeName = entityTypeName;
    this.abstractType = abstractType;
    this.displayName = displayName;
    this.properties = properties;
    this.collectionName = collectionName;
    this.vre = vre;
    this.derivedRelations = derivedRelations;
    this.isRelationCollection = isRelationCollection;
    writeableProperties = properties.entrySet().stream()
      .filter(e -> e.getValue() instanceof LocalProperty)
      .collect(toMap(
        Map.Entry::getKey,
        e -> (LocalProperty) e.getValue(),
        (v1, v2) -> { throw new IllegalStateException("Duplicate key"); },
        LinkedHashMap::new
      ));
  }

  public String getEntityTypeName() {
    return entityTypeName;
  }

  public String getAbstractType() {
    return abstractType;
  }

  public ReadableProperty getDisplayName() {
    return displayName;
  }

  public Map<String, LocalProperty> getWriteableProperties() {
    return writeableProperties;
  }

  public Map<String, ReadableProperty> getReadableProperties() {
    return properties;
  }

  public String getCollectionName() {
    return collectionName;
  }

  public Vre getVre() {
    return vre;
  }

  public Map<String, Supplier<GraphTraversal<Object, Vertex>>> getDerivedRelations() {
    return derivedRelations;
  }

  public boolean isRelationCollection() {
    return isRelationCollection;
  }
  //derivedRelations
}
