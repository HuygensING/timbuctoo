package nl.knaw.huygens.timbuctoo.database.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getProp;

public class RelationType {

  public static final Logger LOG = LoggerFactory.getLogger(RelationType.class);
  private static final ObjectMapper objectMapper = new ObjectMapper();
  private final boolean inverse;
  private final String timId;

  private String regularName;
  private String dbName;
  private String sourceTypeName;
  private String targetTypeName;

  private boolean reflexive;
  private boolean symmetric;
  private boolean derived;

  public static Map<String, RelationType> bothWays(Vertex vertex) {
    return bothWays(
      getProp(vertex, "relationtype_regularName", String.class).orElse("<no name>"),
      getProp(vertex, "relationtype_inverseName", String.class).orElse("<no name>"),
      getProp(vertex, "relationtype_sourceTypeName", String.class).orElse(""),
      getProp(vertex, "relationtype_targetTypeName", String.class).orElse(""),
      getProp(vertex, "relationtype_reflexive", Boolean.class).orElse(false),
      getProp(vertex, "relationtype_symmetric", Boolean.class).orElse(false),
      getProp(vertex, "relationtype_derived", Boolean.class).orElse(false),
      getProp(vertex, "tim_id", String.class).orElse("")
    );
  }

  public static Map<String, RelationType> bothWays(String regularName, String inverseName, String sourceType,
                                                   String targetType, boolean reflexive, boolean symmetric,
                                                   boolean derived, String timid) {
    Map<String, RelationType> result = new HashMap<>();
    result.put(
      regularName,
      new RelationType(regularName, inverseName, sourceType, targetType, reflexive, symmetric, derived, false,
        timid)
    );
    result.put(
      inverseName,
      new RelationType(regularName, inverseName, sourceType, targetType, reflexive, symmetric, derived, true,
        timid)
    );
    return result;
  }

  private RelationType(String regularName, String inverseName, String sourceType, String targetType,
                       boolean reflexive, boolean symmetric, boolean derived, boolean inverse, String timId) {
    this.timId = timId;
    this.inverse = inverse;
    if (inverse) {
      this.regularName = inverseName;
      this.sourceTypeName = targetType;
      this.targetTypeName = sourceType;
      dbName = regularName;
    } else {
      this.regularName = regularName;
      this.sourceTypeName = sourceType;
      this.targetTypeName = targetType;
      dbName = this.regularName;
    }
    this.reflexive = reflexive;
    this.symmetric = symmetric;
    this.derived = derived;
  }

  public boolean isValid(Collection sourceCollection, Collection targetCollection) {
    return (this.sourceTypeName.equals("") || this.sourceTypeName.equals(sourceCollection.getAbstractType())) &&
      (this.targetTypeName.equals("") || this.targetTypeName.equals(targetCollection.getAbstractType()));
  }

  public String getSourceTypeName() {
    return sourceTypeName;
  }

  public String getTargetTypeName() {
    return targetTypeName;
  }

  public String getName() {
    return regularName;
  }

  public String getDbName() {
    return dbName;
  }

  public String getTimId() {
    return timId;
  }

}
