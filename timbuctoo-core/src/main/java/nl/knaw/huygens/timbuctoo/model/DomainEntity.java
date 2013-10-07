package nl.knaw.huygens.timbuctoo.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

// Make sure the '@isWritable'-field is not deserialized.
@JsonIgnoreProperties("@isWritable")
public abstract class DomainEntity extends Entity {

  private Map<String, List<EntityRef>> relations = Maps.newHashMap();

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

}
