package nl.knaw.huygens.timbuctoo.bulkupload;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class RelationDescription {

  public static final Logger LOG = LoggerFactory.getLogger(RelationDescription.class);
  private static final ObjectMapper objectMapper = new ObjectMapper();
  private final boolean inverse;

  private String name;
  private String dbName;
  private String sourceTypeName;
  private String targetTypeName;

  private boolean reflexive;
  private boolean symmetric;
  private boolean derived;

  public static Map<String, RelationDescription> bothWays(Vertex vertex) {
    return bothWays(
      vertex.value("relationtype_regularName"),
      vertex.value("relationtype_inverseName"),
      vertex.value("relationtype_sourceTypeName"),
      vertex.value("relationtype_targetTypeName"),
      vertex.value("relationtype_reflexive"),
      vertex.value("relationtype_symmetric"),
      vertex.value("relationtype_derived")
    );
  }

  public static Map<String, RelationDescription> bothWays(String regularName, String inverseName, String sourceType,
                                                          String targetType, boolean reflexive, boolean symmetric,
                                                          boolean derived) {
    Map<String, RelationDescription> result = new HashMap<>();
    result.put(
      regularName,
      new RelationDescription(regularName, inverseName, sourceType, targetType, reflexive, symmetric, derived, false)
    );
    result.put(
      inverseName,
      new RelationDescription(regularName, inverseName, sourceType, targetType, reflexive, symmetric, derived, true)
    );
    return result;
  }

  private RelationDescription(String regularName, String inverseName, String sourceType, String targetType,
                              boolean reflexive, boolean symmetric, boolean derived, boolean inverse) {
    this.inverse = inverse;
    if (inverse) {
      this.name = inverseName;
      this.sourceTypeName = targetType;
      this.targetTypeName = sourceType;
      dbName = regularName;
    } else {
      this.name = regularName;
      this.sourceTypeName = sourceType;
      this.targetTypeName = targetType;
      dbName = name;
    }
    this.reflexive = reflexive;
    this.symmetric = symmetric;
    this.derived = derived;
  }

  public boolean isValid(Collection sourceCollection, Collection targetCollection) {
    return  this.sourceTypeName.equals(sourceCollection.getAbstractType()) &&
      this.targetTypeName.equals(targetCollection.getAbstractType());
  }

  public Edge makeRelation(Vertex source, Vertex target) {
    if (inverse) {
      return source.addEdge(dbName, target);
    } else {
      return target.addEdge(dbName, source);
    }
  }

  public String getSourceTypeName() {
    return sourceTypeName;
  }

  public String getTargetTypeName() {
    return targetTypeName;
  }

  public String getName() {
    return name;
  }
}
