package nl.knaw.huygens.timbuctoo.search.description.facet;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.model.Datable;
import nl.knaw.huygens.timbuctoo.search.description.FacetDescription;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.text.SimpleDateFormat;
import java.util.List;

public class DateRangeFacetDescription implements FacetDescription {
  public static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyyMMdd");
  private final String facetName;
  private final String propertyName;

  public DateRangeFacetDescription(String facetName, String propertyName) {
    this.facetName = facetName;
    this.propertyName = propertyName;
  }

  @Override
  public Facet getFacet(GraphTraversal<Vertex, Vertex> searchResult) {
    List<Long> dates = Lists.newArrayList();

    searchResult.has(propertyName).forEachRemaining(vertex -> {
      String value = vertex.value(propertyName);

      Datable datable = new Datable(value);

      if (datable.isValid()) {
        dates.add(Long.valueOf(FORMAT.format(datable.getFromDate())));
        dates.add(Long.valueOf(FORMAT.format(datable.getToDate())));
      }
    });

    dates.sort(Long::compareTo);

    // set default values
    Long lowerLimit = 0L;
    Long upperLimit = 0L;
    if (!dates.isEmpty()) {
      lowerLimit = dates.get(0);
      upperLimit = dates.get(dates.size() - 1);
    }

    return new Facet(facetName, Lists.newArrayList(new Facet.RangeOption(lowerLimit, upperLimit)), "RANGE");
  }
}
