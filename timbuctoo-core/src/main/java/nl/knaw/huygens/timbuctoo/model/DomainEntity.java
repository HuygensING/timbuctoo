package nl.knaw.huygens.timbuctoo.model;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

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
  private List<String> variations = Lists.newArrayList();
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

  @JsonProperty("^variations")
  public List<String> getVariations() {
    checkState(variations != null);
    return variations;
  }

  @JsonProperty("^variations")
  public void setVariations(List<String> variations) {
    this.variations = (variations != null) ? variations : Lists.<String> newArrayList();
  }

  public void addVariation(String variation) {
    checkState(variations != null);
    if (!variations.contains(variation)) {
      variations.add(variation);
    }
  }

  @Override
  @JsonProperty("@variationRefs")
  public List<Reference> getVariationRefs() {
    List<Reference> refs = Lists.newArrayListWithCapacity(variations.size());
    for (String variation : variations) {
      refs.add(new Reference(variation, getId()));
    }
    return refs;
  }

  @Override
  @JsonProperty("@variationRefs")
  public void setVariationRefs(List<Reference> variationRefs) {}

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
