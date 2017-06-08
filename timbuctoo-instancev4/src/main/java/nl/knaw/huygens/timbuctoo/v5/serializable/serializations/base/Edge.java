package nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Created on 2017-06-07 10:13.
 */
public class Edge {

  private final String name;
  //private boolean multiEdge;
  private Entity sourceEntity;
  private Entity targetEntity;
  private Object target;
  private String targetType;

  public Edge(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  // public boolean isMultiEdge() {
  //   return multiEdge;
  // }
  //
  // public void setMultiEdge(boolean multiEdge) {
  //   this.multiEdge = multiEdge;
  // }

  public Entity getSourceEntity() {
    return sourceEntity;
  }

  public void setSourceEntity(Entity sourceEntity) {
    this.sourceEntity = sourceEntity;
  }

  public Entity getTargetEntity() {
    return targetEntity;
  }

  public void setTargetEntity(Entity targetEntity) {
    this.targetEntity = targetEntity;
  }

  public Object getTarget() {
    return target;
  }

  public void setTarget(Object target) {
    this.target = target;
  }

  public String getTargetType() {
    return targetType;
  }

  public void setTargetType(String targetType) {
    this.targetType = targetType;
  }

  public boolean isValueEdge() {
    return target != null;
  }

  public boolean isNodeEdge() {
    return targetEntity != null;
  }

  @Nullable
  public String getSourceUri() {
    return sourceEntity == null ? null : sourceEntity.getUri();
  }

  @Nullable
  public String getTargetUri() {
    return targetEntity == null ? null : targetEntity.getUri();
  }

  public Edge copy() {
    Edge edge = new Edge(name);
    //edge.multiEdge = multiEdge;
    edge.sourceEntity = sourceEntity;
    edge.targetEntity = targetEntity;
    edge.target = target;
    edge.targetType = targetType;

    return edge;
  }

  @Override
  public String toString() {
    return super.toString() +
      " [" +
      (sourceEntity == null ? "<>" : sourceEntity.getUri()) +
      " - " + name +
      " -> " +
      (targetEntity == null ? target + (targetType == null ? "" : "(" + targetType + ")") : targetEntity.getUri()) +
      "]";
  }

  @Override
  public int hashCode() {
    return Objects.hash(getSourceUri(), name, getTargetUri(), target);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Edge) {
      Edge other = (Edge) obj;
      return Objects.equals(getSourceUri(), other.getSourceUri()) && Objects.equals(name, other.getName()) &&
        Objects.equals(getTargetUri(), other.getTargetUri());
    } else {
      return false;
    }
  }
}
