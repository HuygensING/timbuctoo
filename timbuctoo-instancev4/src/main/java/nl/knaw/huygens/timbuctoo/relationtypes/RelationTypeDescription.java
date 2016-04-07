package nl.knaw.huygens.timbuctoo.relationtypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.model.Change;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RelationTypeDescription {

  public static final Logger LOG = LoggerFactory.getLogger(RelationTypeDescription.class);
  private static final ObjectMapper objectMapper = new ObjectMapper();

  private String type;
  private Change created;
  private Change modified;
  private int rev;
  private String id;
  private boolean derived;
  private String inverseName;
  private boolean reflexive;
  private String regularName;
  private String sourceTypeName;
  private boolean symmetric;
  private String targetTypeName;

  public RelationTypeDescription(Vertex vertex) {

    setId((String) vertex.property("tim_id").value());
    setType((String) vertex.property("types").value());
    setRegularName((String) vertex.property("relationtype_regularName").value());
    setInverseName((String) vertex.property("relationtype_inverseName").value());

    setSourceTypeName((String) vertex.property("relationtype_sourceTypeName").value());
    setTargetTypeName((String) vertex.property("relationtype_targetTypeName").value());

    setCreated((String) vertex.property("created").value());
    setModified((String) vertex.property("modified").value());
    setRev((Integer) vertex.property("rev").value());
    setReflexive((Boolean) vertex.property("relationtype_reflexive").value());
    setDerived((Boolean) vertex.property("relationtype_derived").value());
    setSymmetric((Boolean) vertex.property("relationtype_symmetric").value());

  }



  @JsonProperty("@type")
  public String getType() {
    return type;
  }

  public void setType(String type) {
    try {
      this.type = objectMapper.readValue(type, String[].class)[0];
    } catch (IOException e) {
      this.type = null;
    }
  }

  @JsonProperty("^created")
  public Change getCreated() {
    return created;
  }

  public void setCreated(String created) {
    try {
      this.created = objectMapper.readValue(created, Change.class);
    } catch (IOException e) {
      this.created = null;
    }
  }

  @JsonProperty("^modified")
  public Change getModified() {
    return modified;
  }

  public void setModified(String modified) {
    try {
      this.modified = objectMapper.readValue(modified, Change.class);
    } catch (IOException e) {
      this.modified = null;
    }
  }

  @JsonProperty("^rev")
  public int getRev() {
    return rev;
  }

  public void setRev(int rev) {
    this.rev = rev;
  }

  @JsonProperty("_id")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public boolean isDerived() {
    return derived;
  }

  public void setDerived(boolean derived) {
    this.derived = derived;
  }

  public String getInverseName() {
    return inverseName;
  }

  public void setInverseName(String inverseName) {
    this.inverseName = inverseName;
  }

  public boolean isReflexive() {
    return reflexive;
  }

  public void setReflexive(boolean reflexive) {
    this.reflexive = reflexive;
  }

  public String getRegularName() {
    return regularName;
  }

  public void setRegularName(String regularName) {
    this.regularName = regularName;
  }

  public String getSourceTypeName() {
    return sourceTypeName;
  }

  public void setSourceTypeName(String sourceTypeName) {
    this.sourceTypeName = sourceTypeName;
  }

  public boolean isSymmetric() {
    return symmetric;
  }

  public void setSymmetric(boolean symmetric) {
    this.symmetric = symmetric;
  }

  public String getTargetTypeName() {
    return targetTypeName;
  }

  public void setTargetTypeName(String targetTypeName) {
    this.targetTypeName = targetTypeName;
  }
}
