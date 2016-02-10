package nl.knaw.huygens.timbuctoo.util;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.collect.Lists;
import org.apache.tinkerpop.gremlin.structure.Edge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;

public class RelationData {
  private Integer rev = null;
  private UUID timId = null;
  private Boolean isLatest = null;
  private List<String> types = null;
  private Map<String, Boolean> accepted = null;
  private Boolean deleted = null;
  private UUID typeId = null;
  private String otherKey = null;

  public RelationData(String otherKey) {
    this.setOtherKey(otherKey);
  }

  public void setRev(Integer rev) {
    this.rev = rev;
  }

  public void setTimId(UUID timId) {
    this.timId = timId;
  }

  public void setIsLatest(Boolean latest) {
    isLatest = latest;
  }

  public void setTypes(List<String> types) {
    this.types = types;
  }

  public void setAccepted(Map<String, Boolean> accepted) {
    this.accepted = accepted;
  }

  public void setDeleted(Boolean deleted) {
    this.deleted = deleted;
  }

  public void setTypeId(UUID typeId) {
    this.typeId = typeId;
  }

  public String getOtherKey() {
    return otherKey;
  }

  public void setOtherKey(String otherKey) {
    this.otherKey = otherKey;
  }

  public void setProperties(Edge vertex) {
    if (rev != null) {
      vertex.property("rev", rev);
    } else {
      vertex.property("rev", 1);
    }
    if (timId != null) {
      vertex.property("tim_id", timId.toString());
    } else {
      vertex.property("tim_id", UUID.randomUUID().toString());
    }
    if (types != null) {
      ArrayNode types = JsonNodeFactory.instance.arrayNode();
      this.types.forEach(type -> types.add(jsn(type + "relation")));
      vertex.property("types", types.toString());
    }
    if (accepted != null) {
      accepted.forEach((key, val) -> {
        vertex.property(key + "_accepted", val);
      });
    }
    if (deleted != null) {
      vertex.property("deleted", deleted);
    }
    if (typeId != null) {
      vertex.property("typeId", typeId);
    } else {
      vertex.property("typeId", UUID.randomUUID().toString());
    }
    if (isLatest != null) {
      vertex.property("isLatest", isLatest);
    } else {
      vertex.property("isLatest", true);
    }
  }

  public static class RelationDataBuilder {
    private String otherKey;
    private Integer rev;
    private UUID timId;
    private List<String> types = Lists.newArrayList();
    private Map<String, Boolean> accepted = new HashMap<>();
    private Boolean deleted;
    private UUID typeId;
    private Boolean isLatest;

    private RelationDataBuilder(String otherKey) {
      this.otherKey = otherKey;
    }

    public static RelationDataBuilder makeRelationData(String otherKey) {
      return new RelationDataBuilder(otherKey);
    }

    public RelationDataBuilder withRev(Integer rev) {
      this.rev = rev;
      return this;
    }

    public RelationDataBuilder withTim_id(UUID timId) {
      this.timId = timId;
      return this;
    }

    public RelationDataBuilder addType(String type) {
      this.types.add(type);
      return this;
    }

    public RelationDataBuilder withAccepted(String type, boolean accepted) {
      this.accepted.put(type, accepted);
      return this;
    }

    public RelationDataBuilder withDeleted(Boolean deleted) {
      this.deleted = deleted;
      return this;
    }

    public RelationDataBuilder withTypeId(UUID typeId) {
      this.typeId = typeId;
      return this;
    }

    public RelationDataBuilder withIsLatest(Boolean isLatest) {
      this.isLatest = isLatest;
      return this;
    }

    RelationData build() {
      RelationData relationData = new RelationData(otherKey);
      relationData.setRev(rev);
      relationData.setTimId(timId);
      if (types.size() > 0) {
        relationData.setTypes(types);
      }
      relationData.setAccepted(accepted);
      relationData.setDeleted(deleted);
      relationData.setTypeId(typeId);
      relationData.setIsLatest(isLatest);
      return relationData;
    }
  }
}
