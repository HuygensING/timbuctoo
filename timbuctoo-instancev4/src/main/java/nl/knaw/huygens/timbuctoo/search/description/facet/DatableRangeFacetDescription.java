package nl.knaw.huygens.timbuctoo.search.description.facet;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import nl.knaw.huygens.timbuctoo.model.Datable;
import nl.knaw.huygens.timbuctoo.search.FacetValue;
import nl.knaw.huygens.timbuctoo.search.description.FacetDescription;
import nl.knaw.huygens.timbuctoo.server.rest.search.DateRangeFacetValue;
import org.apache.commons.lang.StringUtils;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DatableRangeFacetDescription implements FacetDescription {
  public static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyyMMdd");
  public static final Logger LOG = LoggerFactory.getLogger(DatableRangeFacetDescription.class);
  private final String facetName;
  private final String propertyName;

  public DatableRangeFacetDescription(String facetName, String propertyName) {
    this.facetName = facetName;
    this.propertyName = propertyName;
  }

  @Override
  public Facet getFacet(GraphTraversal<Vertex, Vertex> searchResult) {
    List<Long> dates = Lists.newArrayList();

    searchResult.has(propertyName).forEachRemaining(vertex -> {
      Datable datable = getDatable(vertex.value(propertyName));

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

  private Datable getDatable(String datableAsString) {
    String value = StringUtils.strip(datableAsString, "\"");

    return new Datable(value);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void filter(GraphTraversal<Vertex, Vertex> graphTraversal, List<FacetValue> facets) {
    Optional<FacetValue> first = facets.stream()
                                       .filter(facetValue -> Objects.equals(facetValue.getName(), facetName))
                                       .findFirst();
    if (!first.isPresent()) {
      return;
    }
    FacetValue facetValue = first.get();

    if (!(facetValue instanceof DateRangeFacetValue)) {
      return;
    }

    // pad the strings to make them parsable
    String lowerLimitString = Strings.padStart("" + ((DateRangeFacetValue) facetValue).getLowerLimit(), 8, '0');
    String upperLimitString = Strings.padStart("" + ((DateRangeFacetValue) facetValue).getUpperLimit(), 8, '0');

    try {
      Range<Date> range = Range.closed(FORMAT.parse(
        lowerLimitString), FORMAT.parse(upperLimitString));

      graphTraversal.where(__.has(propertyName, P.test((o1, o2) -> {
        Datable datable = getDatable("" + o1);

        Range<Date> range1 = (Range<Date>) o2;
        return range1.contains(datable.getFromDate()) || range1.contains(datable.getToDate());
      }, range)));
    } catch (ParseException e) {
      e.printStackTrace();
    }

  }
}
