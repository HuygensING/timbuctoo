package nl.knaw.huygens.timbuctoo.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public abstract class DomainEntity extends Entity implements Variable {

  public static final String PID = "^pid";
  public static final String DELETED = "^deleted";

  private String pid; // the persistent identifier.
  private boolean deleted;
  private Map<String, List<EntityRef>> relations = Maps.newHashMap();
  protected List<Reference> variations = Lists.newArrayList();
  private List<Role> roles = Lists.newArrayList();

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
  public void setRelations(Map<String, List<EntityRef>> relations) {
    this.relations = checkNotNull(relations);
  }

  public void addRelation(String name, EntityRef ref) {
    List<EntityRef> refs = relations.get(name);
    if (refs == null) {
      refs = Lists.newArrayList();
      relations.put(name, refs);
    }
    refs.add(ref);
  }

  @Override
  @JsonProperty("@variations")
  public List<Reference> getVariations() {
    return variations;
  }

  @Override
  @JsonProperty("@variations")
  public void setVariations(List<Reference> variations) {
    this.variations = checkNotNull(variations);
  }

  public List<Role> getRoles() {
    return roles;
  }

  public void setRoles(List<Role> roles) {
    this.roles = checkNotNull(roles);
  }

  public void addRole(Role role) {
    roles.add(role);
  }

}
