package nl.knaw.huygens.timbuctoo.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.model.Change;
import org.apache.tinkerpop.gremlin.structure.Edge;

import java.util.ArrayList;
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
  private List<String> typesToRemove = new ArrayList<>();
  private Change modfied;
  private Change created;

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

  public void setProperties(Edge edge, List<String> vertexVres) {
    if (rev != null) {
      edge.property("rev", rev);
    } else {
      edge.property("rev", 1);
    }
    if (timId != null) {
      edge.property("tim_id", timId.toString());
    } else {
      edge.property("tim_id", UUID.randomUUID().toString());
    }
    //types contains the explicitly configured vres
    ArrayNode types = JsonNodeFactory.instance.arrayNode();
    for (String vre : vertexVres) {
      if (!typesToRemove.contains(vre)) {
        types.add(jsn(vre + "relation"));
      }
    }
    if (this.types != null) {
      for (String vre : this.types) {
        if (!typesToRemove.contains(vre)) {
          types.add(jsn(vre + "relation"));
        }
      }
    }
    edge.property("types", types.toString());
    if (accepted != null) {
      accepted.forEach((key, val) -> {
        edge.property(key + "_accepted", val);
      });
    }
    if (deleted != null) {
      edge.property("deleted", deleted);
    }
    if (typeId != null) {
      edge.property("typeId", typeId);
    } else {
      edge.property("typeId", UUID.randomUUID().toString());
    }
    if (isLatest != null) {
      edge.property("isLatest", isLatest);
    } else {
      edge.property("isLatest", true);
    }
    if (created != null) {
      edge.property("created", changeAsString(created));
    }
    if (modfied != null) {
      edge.property("modified", changeAsString(modfied));
    }
  }

  private String changeAsString(Change change) {
    try {
      return new ObjectMapper().writeValueAsString(change);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public void setTypesToRemove(List<String> typesToRemove) {
    this.typesToRemove = typesToRemove;
  }

  private void setModfied(Change modfied) {
    this.modfied = modfied;
  }

  private void setCreated(Change created) {
    this.created = created;
  }

  public static class RelationDataBuilder {
    private String otherKey;
    private Integer rev;
    private UUID timId;
    private List<String> types = Lists.newArrayList();
    private List<String> typesToRemove = Lists.newArrayList();
    private Map<String, Boolean> accepted = new HashMap<>();
    private Boolean deleted;
    private UUID typeId;
    private Boolean isLatest;
    private Change created;
    private Change modified;

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

    public RelationDataBuilder removeType(String type) {
      this.typesToRemove.add(type);
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

    public RelationDataBuilder withCreated(Change created) {
      this.created = created;
      return this;
    }

    public RelationDataBuilder withModified(Change modified) {
      this.modified = modified;
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
      relationData.setTypesToRemove(typesToRemove);
      relationData.setCreated(created);
      relationData.setModfied(modified);
      return relationData;
    }

  }
}
