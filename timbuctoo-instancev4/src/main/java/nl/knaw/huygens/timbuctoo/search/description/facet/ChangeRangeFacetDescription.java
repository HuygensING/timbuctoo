package nl.knaw.huygens.timbuctoo.search.description.facet;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.search.description.FacetDescription;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.shaded.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

class ChangeRangeFacetDescription implements FacetDescription {
  public static final Logger LOG = LoggerFactory.getLogger(ChangeRangeFacetDescription.class);
  public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
  private final String facetName;
  private final String propertyName;
  private final ObjectMapper objectMapper;

  public ChangeRangeFacetDescription(String facetName, String propertyName) {
    this.facetName = facetName;
    this.propertyName = propertyName;
    objectMapper = new ObjectMapper();
  }

  @Override
  public Facet getFacet(GraphTraversal<Vertex, Vertex> searchResult) {
    List<Long> dateStamps = Lists.newArrayList();
    searchResult.has(propertyName).map(vertexTraverser -> vertexTraverser.get().<String>value(propertyName))
      .<String>forEachRemaining(prop -> {
        try {
          Change change = objectMapper.readValue(prop, Change.class);
          Date date = new Date(change.getTimeStamp());
          dateStamps.add(Long.valueOf(DATE_FORMAT.format(date)));
        } catch (IOException e) {
          LOG.error("'{}' is not a valid change.", prop);
        }
      });

    dateStamps.sort(Long::compareTo);


    long lowerLimit = 0;
    long upperLimit = 0;
    if (!dateStamps.isEmpty()) {
      lowerLimit = dateStamps.get(0);
      upperLimit = dateStamps.get(dateStamps.size() - 1);
    }

    return new Facet(facetName, Lists.newArrayList(new Facet.RangeOption(lowerLimit, upperLimit)), "RANGE");
  }
}
