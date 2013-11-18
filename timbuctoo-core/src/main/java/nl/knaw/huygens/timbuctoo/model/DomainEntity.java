package nl.knaw.huygens.timbuctoo.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

// Make sure the '@isWritable'-field is not deserialized.
@JsonIgnoreProperties("@isWritable")
public abstract class DomainEntity extends Entity implements Variable {

  public static final String PID = "^pid";
  public static final String DELETED = "^deleted";

  private String pid; // the persistent identifier.
  private boolean deleted;
  private Map<String, List<EntityRef>> relations = Maps.newHashMap();
  protected List<Reference> variations = Lists.newArrayList();
  protected String currentVariation;
  private List<Role> roles;

  @JsonProperty(PID)
  public String getPid() {
    return pid;
  }

  @JsonProperty(PID)
  public void setPid(String pid) {
    this.pid = pid;
  }

  @JsonProperty(DELETED)
  public boolean isDeleted() {
    return deleted;
  }

  @JsonProperty(DELETED)
  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  @JsonProperty("@relations")
  public Map<String, List<EntityRef>> getRelations() {
    return relations;
  }

  @JsonProperty("@relations")
  public void setRelations(Map<String, List<EntityRef>> variations) {
    this.relations = variations;
  }

  public void addRelation(String name, EntityRef ref) {
    List<EntityRef> refs = relations.get(name);
    if (refs == null) {
      refs = Lists.newArrayList();
      relations.put(name, refs);
    }
    refs.add(ref);
  }

  /**
   * This method returns if the current object is writable. 
   * The value will be serialized, but will not be deserialized. 
   * Because it is dependent on the pid-field.
   * @return
   */
  @JsonProperty("@isWritable")
  public boolean isWritable() {
    /* Only DomainDocuments with a persistent identifier should be writable. 
     * DomainDocuments without one are just there to give the users an opportunity 
     * to check the batch-imported data.
     */
    return getPid() != null;
  }

  @Override
  @JsonProperty("@variations")
  public List<Reference> getVariations() {
    return variations;
  }

  @Override
  @JsonProperty("@variations")
  public void setVariations(List<Reference> variations) {
    this.variations = variations;
  }

  @Override
  public void addVariation(Class<? extends Entity> refType, String refId) {
    variations.add(new Reference(refType, refId));
  }

  @Override
  @JsonProperty("!currentVariation")
  public String getCurrentVariation() {
    return currentVariation;
  }

  @Override
  @JsonProperty("!currentVariation")
  public void setCurrentVariation(String currentVariation) {
    this.currentVariation = currentVariation;
  }

  public List<Role> getRoles() {
    return roles;
  }

  public void setRoles(List<Role> roles) {
    this.roles = roles;
  }

}
