package nl.knaw.huygens.solr;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;

public class FacetParameter {

  String name = "";
  List<String> values = Lists.newArrayList();

  public String getName() {
    return name;
  }

  public FacetParameter setName(String name) {
    this.name = name;
    return this;
  }

  public List<String> getValues() {
    return values;
  }

  public FacetParameter setValues(List<String> values) {
    this.values = values;
    return this;
  }

  public List<String> getEscapedValues() {
    Builder<String> builder = ImmutableList.builder();
    for (String value : getValues()) {
      builder.add(SolrUtils.escapeFacetId(value));
    }
    return builder.build();
  }
}
