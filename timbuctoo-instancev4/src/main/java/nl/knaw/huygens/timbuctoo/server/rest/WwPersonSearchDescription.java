package nl.knaw.huygens.timbuctoo.server.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.server.rest.LocationNames.LocationType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
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
    setModifiedDate(vertex, data);
    setResidenceLocation(vertex, data);
    ref.setData(data);

    return ref;
  }

  private void setResidenceLocation(Vertex vertex, Map<String, Object> data) {
    Iterator<Vertex> residenceLocations = vertex.vertices(Direction.OUT, "hasResidenceLocation");
    StringBuilder sb = new StringBuilder();
    for (; residenceLocations.hasNext(); ) {
      Vertex location = residenceLocations.next();

      String names = getValueAsString(location, "names");
      String locationType = getValueAsString(location, "locationType");


      if (names != null && locationType != null) {
        if (sb.length() > 0) {
          sb.append(";");
        }
        try {

          LocationNames names1 = objectMapper.readValue(names, LocationNames.class);
          sb.append(names1.getDefaultName(objectMapper.readValue(locationType, LocationType.class)));

        } catch (IOException e) {
          LOG.error("Could not convert 'location name' with value '{}'", names);
          LOG.error("Exception throw", e);
        }
      }
      if (sb.length() > 0) {
        data.put("residenceLocation", sb.toString());
      }
    }
    if (!data.containsKey("residenceLocation")) {
      data.put("residenceLocation", null);
    }
  }

  private void setModifiedDate(Vertex vertex, Map<String, Object> data) {
    String modified = getValueAsString(vertex, "modified");
    if (modified != null) {
      try {
        Change change = objectMapper.readValue(modified, Change.class);
        Date date = new Date(change.getTimeStamp());
        data.put("modified_date", new SimpleDateFormat("yyyyMMdd").format(date));
      } catch (IOException e) {
        LOG.error("'modified' could not be read.", e);
        data.put("modified_date", null);
      }
    } else {
      data.put("modified_date", null);
    }
  }

  private void setGender(Map<String, Object> data, Vertex vertex) {
    data.put("gender", getValueAsString(vertex, "wwperson_gender"));
  }

  private String getValueAsString(Vertex vertex, String propertyName) {
    String value = null;
    if (vertex.keys().contains(propertyName)) {
      value = vertex.value(propertyName);
    }
    return value;
  }

  private void setDate(Vertex vertex, Map data, String sourceProperty, String targetProperty) {
    String property = getValueAsString(vertex, sourceProperty);

    if (property != null) {
      data.put(targetProperty, "" + new Datable(property).getFromYear());
    } else {
      data.put(targetProperty, null);
    }
  }

  private void setDisplayName(Vertex vertex, EntityRef ref) {
    String names = getValueAsString(vertex, "wwperson_names");
    if (names != null) {
      try {
        ref.setDisplayName(objectMapper.readValue(names, Names.class)
                                       .defaultName()
                                       .getShortName());
      } catch (IOException e) {
        LOG.error("'names' could not be read.", e);
      }
    } else {
      String tempName = getValueAsString(vertex, "wwperson_tempName");
      if (tempName != null) {
        ref.setDisplayName(tempName);
      }
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
