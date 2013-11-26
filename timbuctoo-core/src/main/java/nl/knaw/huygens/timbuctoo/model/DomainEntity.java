package nl.knaw.huygens.timbuctoo.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public abstract class DomainEntity extends Entity implements Variable {

  public static final String PID = "^pid";
  public static final String DELETED = "^deleted";
  public static final String VARIATIONS = "^variations";

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

  @JsonProperty(VARIATIONS)
  @JsonIgnore
  public List<String> getVariations() {
    return variations;
  }

  @JsonProperty(VARIATIONS)
  @JsonIgnore
  public void setVariations(List<String> variations) {
    this.variations = Lists.newArrayList();
    if (variations != null) {
      for (String variation : variations) {
        addVariation(variation);
      }
    }
  }

  @JsonIgnore
  public void addVariation(String variation) {
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
