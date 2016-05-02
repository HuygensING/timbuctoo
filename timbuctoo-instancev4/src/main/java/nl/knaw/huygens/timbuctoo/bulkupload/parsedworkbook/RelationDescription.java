package nl.knaw.huygens.timbuctoo.bulkupload.parsedworkbook;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getProp;

public class RelationDescription {
  private final String regularName;
  private final String inverseName;
  private final String sourceTypeName;
  private final String targetTypeName;
  private final String id;

  public RelationDescription(Vertex sourceData) {
    regularName = getProp(sourceData, "relationtype_regularName", String.class)
      .orElseThrow(() -> new RuntimeException("no regularname for " + sourceData.id()));
    inverseName = getProp(sourceData, "relationtype_inverseName", String.class)
      .orElseThrow(() -> new RuntimeException("no inverseName for " + sourceData.id()));
    sourceTypeName = getProp(sourceData, "relationtype_sourceTypeName", String.class)
      .orElseThrow(() -> new RuntimeException("no sourceTypeName for " + sourceData.id()));
    targetTypeName = getProp(sourceData, "relationtype_targetTypeName", String.class)
      .orElseThrow(() -> new RuntimeException("no targetTypeName for " + sourceData.id()));
    id = getProp(sourceData, "tim_id", String.class)
      .orElseThrow(() -> new RuntimeException("no tim_id for " + sourceData.id()));
  }

  public String getRegularName() {
    return regularName;
  }

  public String getInverseName() {
    return inverseName;
  }

  public String getSourceTypeName() {
    return sourceTypeName;
  }

  public String getTargetTypeName() {
    return targetTypeName;
  }

  public String getId() {
    return id;
  }
}
