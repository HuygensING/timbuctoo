package nl.knaw.huygens.timbuctoo.server.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class WwPersonSearchDescription {
  public static final Logger LOG = LoggerFactory.getLogger(WwPersonSearchDescription.class);
  private ObjectMapper objectMapper;

  private static final List<String> SORTABLE_FIELDS = Lists.newArrayList(
    "dynamic_k_modified",
    "dynamic_k_birthDate",
    "dynamic_sort_name",
    "dynamic_k_deathDate");

  private static final List<String> FULL_TEXT_SEARCH_FIELDS = Lists.newArrayList(
    "dynamic_t_tempspouse",
    "dynamic_t_notes",
    "dynamic_t_name");
  public static final String ID_DB_PROP = "tim_id";
  private final String type = "wwperson";

  public WwPersonSearchDescription() {
    objectMapper = new ObjectMapper();
  }

  public List<String> getSortableFields() {
    return SORTABLE_FIELDS;
  }

  public List<String> getFullTextSearchFields() {
    return FULL_TEXT_SEARCH_FIELDS;
  }

  public TimbuctooQuery createQuery(SearchRequestV2_1 searchRequest) {
    return new TimbuctooQuery(this);
  }

  public EntityRef createRef(Vertex vertex) {
    EntityRef ref = new EntityRef(type, vertex.value(ID_DB_PROP));
    setDisplayName(vertex, ref);

    Map<String, Object> data = Maps.newHashMap();
    data.put("_id", vertex.value(ID_DB_PROP));
    data.put("name", ref.getDisplayName());
    setDate(vertex, data, "wwperson_birthDate", "birthDate");
    setDate(vertex, data, "wwperson_deathDate", "deathDate");
    setGender(data, vertex);
    ref.setData(data);

    return ref;
  }

  private void setGender(Map<String, Object> data, Vertex vertex) {
    if (vertex.keys().contains("wwperson_gender")) {
      data.put("gender", vertex.value("wwperson_gender"));
    } else {
      data.put("gender", null);
    }
  }

  private void setDate(Vertex vertex, Map data, String sourceProperty, String targetProperty) {
    if (vertex.keys().contains(sourceProperty)) {
      data.put(targetProperty, "" + new Datable(vertex.value(sourceProperty)).getFromYear());
    } else {
      data.put(targetProperty, null);
    }
  }

  private void setDisplayName(Vertex vertex, EntityRef ref) {
    if (vertex.keys().contains("wwperson_names")) {
      String names = vertex.value("wwperson_names");
      try {
        ref.setDisplayName(objectMapper.readValue(names, Names.class)
                                       .defaultName()
                                       .getShortName());
      } catch (IOException e) {
        LOG.error("'names' could not be read.", e);
      }
    } else if (vertex.keys().contains("wwperson_tempName")) {
      ref.setDisplayName(vertex.value("wwperson_tempName"));
    }
  }

  public GraphTraversal<Vertex, Vertex> filterByType(GraphTraversal<Vertex, Vertex> vertices) {
    return vertices.filter(x -> ((String) x.get().property("types").value()).contains(type));
  }

  String getType() {
    return type;
  }

  static class Names {
    public List<PersonName> list;

    public Names() {
      list = Lists.newArrayList();
    }

    public PersonName defaultName() {
      return (list != null && !list.isEmpty()) ? list.get(0) : new PersonName();
    }

    @Override
    public boolean equals(Object obj) {
      return EqualsBuilder.reflectionEquals(this, obj, false);
    }

    @Override
    public int hashCode() {
      return HashCodeBuilder.reflectionHashCode(this, false);
    }
  }
}
