package nl.knaw.huygens.repository.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public abstract class DomainDocument extends Document {

  private Map<String, List<DocumentRef>> relations = Maps.newHashMap();

  @JsonProperty("@relations")
  public Map<String, List<DocumentRef>> getRelations() {
    return relations;
  }

  @JsonProperty("@relations")
  public void setRelations(Map<String, List<DocumentRef>> variations) {
    this.relations = variations;
  }

  public void addRelation(String name, DocumentRef ref) {
    List<DocumentRef> refs = relations.get(name);
    if (refs == null) {
      refs = Lists.newArrayList();
      relations.put(name, refs);
    }
    refs.add(ref);
  }

}
