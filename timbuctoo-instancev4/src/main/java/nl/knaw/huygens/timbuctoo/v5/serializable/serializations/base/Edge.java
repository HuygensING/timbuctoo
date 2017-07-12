package nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Created on 2017-06-07 10:13.
 */
public class Edge {

  private final String id;
  private final String name;
  private boolean multiple;
  private Entity sourceEntity;
  private Entity targetEntity;
  private Object target;
  private String targetType;

  public Edge(String name, int index) {
    this.name = name;
    this.id = "e" + index;
  }

  public Edge(String name) {
    this(name, -1);
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public boolean isMultiple() {
    return multiple;
  }

  public void setMultiple(boolean multiple) {
    this.multiple = multiple;
  }

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

  public Edge copy(int index) {
    Edge edge = new Edge(name, index);
    edge.multiple = multiple;
    edge.sourceEntity = sourceEntity;
    edge.targetEntity = targetEntity;
    edge.target = target;
    edge.targetType = targetType;
    return edge;
  }

  public String getTargetString() {
    return (targetEntity == null ? target + (targetType == null ? "" : "(" + targetType + ")") : targetEntity.getUri());
  }

  public Object getTargetObject() {
    return (targetEntity == null ? target : targetEntity.getUri());
  }

  public String getTargetAsString() {
    return (targetEntity == null ? target == null ? "" : target.toString() : targetEntity.getUri());
  }

  @Override
  public String toString() {
    return //super.toString() +
      "<(" + id + ") " +
      (sourceEntity == null ? "<>" : sourceEntity.getUri()) +
      " --" + name +
      "--> " +
      getTargetString() +
      ">";
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
